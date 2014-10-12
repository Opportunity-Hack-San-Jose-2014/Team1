package com.ebay.ohack2014;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/**
 * @author vinay nagar
 * http://localhost:8080/ohack2014/spendaday/v1/*
 *
 */
@Path("v1") // Version 1 Service
public class SpendADayService {
	
	@GET
	@Path("/documents")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDocuments() {
		MongoClient mongoClient = MongoClientProvider.getMongoClient();
		DB db = mongoClient.getDB("test");
		DBCollection coll = db.getCollection("test");
		DBCursor cursor = coll.find();
		String serialize = JSON.serialize(cursor);

		return serialize;
	}
	
	
	/*
	 * SJPL Public APIS
	 *
	 */
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("keyword") String keyword,
						 @QueryParam("collection") String collection,
						 @QueryParam("location") String location,
						 @QueryParam("year") String year,
						 @QueryParam("page") int page,
						 @QueryParam("limit") int limit,
						 @Context HttpServletResponse responseContext) {
		boolean failure = false;
		JSONArray docs = new JSONArray();
		JSONArray collectionFilters = new JSONArray();
		Set<String> autoComplete = new HashSet<String>();
		Set<String> collectionSet = new HashSet<String>();
		ArrayList<Integer> years = new ArrayList<Integer>();
		JSONObject yearsRange = new JSONObject();
		boolean okToFail = false;

		// Find the results by keywords
		if (okToFail && keyword == null && collection == null && location == null
				&& year == null) {
			failure = true;
		} else {
			MongoClient mongoClient = MongoClientProvider.getMongoClient();
			DB db = mongoClient.getDB("ohack2014");
			DBCollection coll = db.getCollection("items");
			
			DBObject query = new BasicDBObject();
			
			if (keyword != null) {
				Pattern regex = Pattern.compile(keyword.toLowerCase(), Pattern.CASE_INSENSITIVE);
				//Pattern regex = Pattern.compile(".*hall.*", Pattern.CASE_INSENSITIVE);
//				DBObject clause1 = new BasicDBObject("title", regex);  
//				DBObject clause2 = new BasicDBObject("description", regex);
//				DBObject clause3 = new BasicDBObject("collection", regex);
//				DBObject clause4 = new BasicDBObject("subject", regex);
//				DBObject clause5 = new BasicDBObject("relation", regex);
//				
//				BasicDBList or = new BasicDBList();
//				or.add(clause1);
//				or.add(clause2);
//				or.add(clause3);
//				or.add(clause4);
//				or.add(clause5);
//				query = new BasicDBObject("$or", or);
				query.put("title", regex);
			}
			
			if (year != null) {
				if (year.contains("-")) {
					String[] yearRange = year.split("-");
					int a = Integer.parseInt(yearRange[0]);
					int b = Integer.parseInt(yearRange[1]);
					query.put("date", new BasicDBObject("$gt", a).append("$lte",b));
				} else {
					query.put("date", Integer.parseInt(year));
				}
			}
			
			if (location != null && !location.isEmpty()) {
				if (location.contains(",")) {
					String[] latLon = location.split(",");
					query.put("lat", latLon[0]);
					query.put("lon", latLon[1]);
				} 
			}
			
			if (collection != null && !collection.isEmpty()) {
				query.put("collection", collection);
			}
			
			if (!(page > 0)) {
				page = 0;
			}
			
			if (!(limit > 0)) {
				limit = 40;
			}
			
			DBCursor cursor;
			if (keyword == null && collection == null && location == null
					&& year == null) {
				cursor = coll.find();
			} else {
				cursor = coll.find(query);
			}
		
	
			try {
				while (cursor.hasNext() && limit > 0) {
					BasicDBObject dbObj = (BasicDBObject) cursor.next();
					JSONObject doc = new JSONObject();
					
					if (dbObj.getString("title") != null) {
						doc.put("title", dbObj.getString("title"));
						doc.put("image", dbObj.getString("image_url"));
						doc.put("lat", dbObj.getString("lat"));
						doc.put("lon", dbObj.getString("lon"));
						doc.put("year", dbObj.getString("date"));
						doc.put("id", dbObj.getString("id"));
						docs.put(doc);
						autoComplete.add(dbObj.getString("title"));
						limit--;
					}
					
					JSONObject collectionFilter = new JSONObject();
					if (collectionSet.add(dbObj.getString("collection"))) {
						collectionFilter.put("name", dbObj.getString("collection"));
						//collectionFilter.put("image", getImageByCollection(dbObj.getString("collection")));
						collectionFilter.put("image", "http://upload.wikimedia.org/wikipedia/commons/1/1e/Stonehenge.jpg");
						collectionFilters.put(collectionFilter);
					}

					years.add(Integer.parseInt(dbObj.getString("date")));
				}
			} catch (JSONException e) {
				failure = true;
				e.printStackTrace();
			} finally {
				cursor.close();
			}
			
			Collections.sort(years);
			
			try {
				if (years.size() > 0) {
					yearsRange.put("yearmin", years.get(0));
					yearsRange.put("yearmax", years.get(years.size() - 1));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		JSONObject response = new JSONObject();
		try {
			if (!failure) {
				response.put("ack", "success");
				response.put("docs", docs);
				response.put("suggestions", getSuggestions(keyword));
				response.put("collections", collectionFilters);
				response.put("autocomplete", autoComplete);
				response.put("years", yearsRange);
			} else {
				response.put("ack", "failure");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}

	@GET
	@Path("/relatedsearch")
	@Produces(MediaType.APPLICATION_JSON)
	public String relatedSearch(@QueryParam("keyword") String keyword,
				@Context HttpServletResponse responseContext) {
		boolean failure = false;
		
		if (keyword == null) {
			failure = true;
		}
		
		JSONObject response = new JSONObject();
		
		try {
			if (!failure) {
				response.put("ack", "success"); 
				response.put("suggestions", getSuggestions("test"));
			} else {
				response.put("ack", "failure");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}
	
	@GET
	@Path("/collections")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollections(@Context HttpServletResponse responseContext) {
		boolean failure = false;
		MongoClient mongoClient = MongoClientProvider.getMongoClient();
		DB db = mongoClient.getDB("test");
		DBCollection coll = db.getCollection("test");
		DBCursor cursor = coll.find();
		
		JSONArray collections = new JSONArray();

		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				JSONObject collection = new JSONObject();
				collection.put("title", dbObj.getString("title"));
				collection.put("image", dbObj.getString("image"));
				collections.put(collection);
			}
		} catch (JSONException e) {
			failure = true;
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
	JSONObject response = new JSONObject();
	try {
		if (!failure) {
			response.put("ack", "success");
			response.put("collections", collections);
		} else {
			response.put("ack", "failure");
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}

	responseContext.setHeader("Access-Control-Allow-Origin", "*");
	return response.toString();
	
	}
	
	@GET
	@Path("/item/{itemid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getItem(@PathParam("itemid") String itemId,
			@Context HttpServletResponse responseContext) {
		boolean failure = false;
		MongoClient mongoClient = MongoClientProvider.getMongoClient();
		DB db = mongoClient.getDB("test");
		DBCollection coll = db.getCollection("test");
		DBObject query = new BasicDBObject();
		query.put("id", Integer.parseInt(itemId));
		DBCursor cursor = coll.find(query);
		
		JSONArray collections = new JSONArray();
		
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				JSONObject collection = new JSONObject();
				collection.put("title", dbObj.getString("title"));
				collection.put("image", dbObj.getString("image"));
				collection.put("description", dbObj.getString("description"));
				collection.put("creator", dbObj.getString("creator"));
				collection.put("contributor", dbObj.getString("contibutor"));
				collection.put("language", dbObj.getString("language"));
				collection.put("year", dbObj.getString("date"));
				collection.put("cataloger", dbObj.getString("cataloger"));
				collection.put("location", dbObj.getString("location"));
				collections.put(collection);
			}
		} catch (JSONException e) {
			failure = true;
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
	JSONObject response = new JSONObject();
	try {
		if (!failure) {
			response.put("ack", "success");
			response.put("collections", collections);
		} else {
			response.put("ack", "failure");
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}

	responseContext.setHeader("Access-Control-Allow-Origin", "*");
	return response.toString();
	}
	
	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/item/{itemid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateItem(@PathParam("itemid") String itemId,
						String keyValueJson,
						@Context HttpServletResponse responseContext) {
		MongoClient mongoClient = MongoClientProvider.getMongoClient();
		JSONObject response = new JSONObject();
		
		try {
			KeyValue keyValue = new ObjectMapper().readValue(keyValueJson, KeyValue.class);
			DB db = mongoClient.getDB("test");
			DBCollection coll = db.getCollection("test");
			DBObject query = new BasicDBObject();
			query.put("title", "test123");
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", new BasicDBObject().append(keyValue.getkey(), keyValue.getvalue()));
			coll.update(query, updateQuery);
			response.put("ack", "sucesss");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}
	
	@GET
	@Path("/histogram")
	@Produces(MediaType.APPLICATION_JSON)
	public String histogram(@QueryParam("query") String query,
						@Context HttpServletResponse responseContext) {
		DBObject groupFields = new BasicDBObject( "_id", "$query");
	    groupFields.put("count", new BasicDBObject( "$sum", 1));
	    DBObject group = new BasicDBObject("$group", groupFields );
	    
	    MongoClient mongoClient = MongoClientProvider.getMongoClient();
		DB db = mongoClient.getDB("test");
		DBCollection coll = db.getCollection("test");
	    
	    AggregationOutput output = coll.aggregate(group);
	    return "";
	    //output.
	}
	/*
	 * Internal SJPL methods
	 */
	private Set<String> getSuggestions(String keyword) {
		URL url;
		JSONObject response = new JSONObject();
		boolean failure = false;
		String name;
		Set<String> suggestions = new HashSet<String>();
		int limit = 10;
		try {            
			url = new URL("https://www.googleapis.com/freebase/v1/search?query=" 
					+ keyword + "&domain=history&key=AIzaSyAvJzm4A1hlvVrBYz8NvV9k3Vxaij-AYKA&limit=" 
					+ limit);
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			String jsonString = writer.toString();
			JSONObject jsonObject = new JSONObject(jsonString);
			
			for (int i = 0; i < limit; i++) {
				name = (String) jsonObject.getJSONArray("result").getJSONObject(i).get("name");
				suggestions.add(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
			failure = true;
		}
		
		try {
			if (!failure) {
				response.put("ack", "success");
				response.put("suggestions", suggestions);
			} else {
				response.put("ack", "failure");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return suggestions;
	}
	
	private String getImageByCollection (String collection) {
		MongoClient mongoClient = MongoClientProvider.getMongoClient();
		DB db = mongoClient.getDB("test");
		DBCollection coll = db.getCollection("test");
		
		DBObject query = new BasicDBObject();
		query.put("collection", collection);
		
		DBCursor cursor = coll.find(query);
		String image = "";
		
		try {
			if (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				image = dbObj.getString("image");
			}
		} finally {
			cursor.close();
		}
		
		return image;
	}
	
	private String getGeo(long zip) {
		URL url;
		JSONObject response = new JSONObject();
		boolean failure = false;
		double lat = 0, lng = 0;
		
		try {
			url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + zip + "&key=AIzaSyAvJzm4A1hlvVrBYz8NvV9k3Vxaij-AYKA");
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			String jsonString = writer.toString();
			JSONObject jsonObject = new JSONObject(jsonString);
			lat = (double) jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lat");
			lng = (double) jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").get("lng");
		} catch (Exception e) {
			e.printStackTrace();
			failure = true;
		}
		
		try {
			if (!failure) {
				response.put("ack", "success");
				JSONObject loc = new JSONObject();
				loc.put("lat", lat);
				loc.put("lng", lng);
				response.put("loc", loc);
			} else {
				response.put("ack", "failure");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return response.toString();
	}
	
}





