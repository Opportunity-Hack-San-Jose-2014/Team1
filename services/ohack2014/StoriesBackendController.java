package com.stories.backend;

import java.awt.Desktop;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

@Path("controller")
public class StoriesBackendController {
	static String ENDPOINT = "http://apilisting.qa.ebay.com/services/listing/ListingDraftService/v1";
	static String SERVERURI = "http://www.ebay.com/marketplace/listing/v1/services";
	static float HUE_X  = 0;
	static float HUE_Y  = 0;

	private Map<String, String> pageIdToBaseURL = new HashMap<String, String>();
	DB db = null;
	
	public enum ServiceConstants {
		createListingDraft
	}

	public static Map<String, ServiceConstants> serviceMap = new HashMap<String, ServiceConstants>();

	static {
		serviceMap.put("createListingDraft",
				ServiceConstants.createListingDraft);
	}

	public StoriesBackendController() {
		//mongoClient = new MongoClient("127.0.0.1", 27017);
		//
		pageIdToBaseURL.put("drafts", "http://sm.ebay.com");
		pageIdToBaseURL.put("viewItem", "http://www.ebay.com");
		pageIdToBaseURL.put("srp", "http://www.ebay.com");
	}

	/* ebay skunkworks */
	@GET
	@Path("/documents")
	public String getDocuments() {
		DBCollection coll = db.getCollection("test");

		DBCursor cursor = coll.find();
		String serialize = JSON.serialize(cursor);

		return serialize;
	}

	@POST
	@Path("/registerdevice/{ebayId}/deviceid/{deviceId}/devicetype/{deviceType}")
	@Produces(MediaType.APPLICATION_JSON)
	public String registerDevice(@PathParam("ebayId") String ebayid,
			@PathParam("deviceId") String deviceid,
			@PathParam("deviceType") String deviceType,
			@DefaultValue("") @QueryParam("desc") String desc,
			@Context HttpServletResponse responseContext) {
		MongoClient mongoClient = MongoDBHelper.getInstance().getMongoClient();
		DB db = mongoClient.getDB("whoosh");
		DBCollection coll = db.getCollection("devices");

		// insert Data
		/*
		 * { "ebayid" : "MongoDB", "deviceid" : "database", type: "mobile",
		 * "desc" : 1
		 * 
		 * }
		 */
		BasicDBObject doc = new BasicDBObject("ebayId", ebayid).append(
				"deviceId", deviceid).append("desc", desc).append("deviceType", deviceType);

		coll.insert(doc);

		JSONObject response = new JSONObject();
		try {
			response.put("ack", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}

	@GET
	@Path("/getregistereddevices/{ebayId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getregisteredDevices(@PathParam("ebayId") String ebayId,
			@Context HttpServletResponse responseContext) {
		MongoClient mongoClient = MongoDBHelper.getInstance().getMongoClient();
		JSONArray devices = new JSONArray();
		DB db = mongoClient.getDB("whoosh");
		DBCollection coll = db.getCollection("devices");

		// get Data
		/*
		 * { "ebayid" : "MongoDB", "deviceid" : "database", type: "mobile",
		 * "desc" : 1
		 * 
		 * }
		 */
		// find all where i > 50
		BasicDBObject query = new BasicDBObject("ebayId", ebayId);

		DBCursor cursor = coll.find(query);

		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				JSONObject device = new JSONObject();
				device.put("ebayId", dbObj.getString("ebayId"));
				device.put("deviceId", dbObj.getString("deviceId"));
				device.put("deviceType", dbObj.getString("deviceType"));
				device.put("desc", dbObj.getString("desc"));
				devices.put(device);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		JSONObject response = new JSONObject();
		try {
			response.put("ack", "success");
			response.put("devices", devices);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}

	@GET
	@Path("/setcontext/{ebayId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String setContext(@QueryParam("whooshJson") String whooshJson,
			@PathParam("ebayId") String ebayId,
			@Context HttpServletResponse responseContext) {

		/*
		 * whooshObj is a json. It must have a 'context' key which represents
		 * the actual context.
		 */
		MongoClient mongoClient = MongoDBHelper.getInstance().getMongoClient();
		String deviceId = null, targetDeviceId = null, pageId = null, finalURL = null, contextString = null, popup = null, later = null, name = null;

		JSONObject jsonObj, contextObj = null;

		try {
			jsonObj = new JSONObject(whooshJson);
			JSONObject whooshContextObj = jsonObj
					.getJSONObject("whooshContext");
			deviceId = whooshContextObj.getString("deviceId");
			targetDeviceId = whooshContextObj.getString("targetDeviceId");
			pageId = whooshContextObj.getString("pageId");
			finalURL = whooshContextObj.getString("finalURL");
			popup = whooshContextObj.getString("popup");
			later = whooshContextObj.getString("later");
			name = whooshContextObj.getString("name");
			contextObj = whooshContextObj.getJSONObject("context");
			if (contextObj != null) {	
				contextString = contextObj.toString();
			}
		} catch (JSONException e1) {
			//e1.printStackTrace();
		}

		DB db = mongoClient.getDB("whoosh");
		DBCollection coll = db.getCollection("whooshcontext");

		// insert Data : Map of values
		/*
		 * { "ebayid" : "MongoDB", "deviceid" : "database"} and all the other
		 * fields
		 */

		BasicDBObject doc = new BasicDBObject("ebayId", ebayId)
				.append("deviceId", deviceId)
				.append("targetDeviceId", targetDeviceId)
				.append("finalURL", finalURL)
				.append("pageId", pageId)
				.append("context", contextString)
				.append("baseURL", pageIdToBaseURL.get(pageId))
				.append("sent", "false")
				.append("popup", popup)
				.append("later", later)
				.append("name", name)
				.append("time", new Date().getTime());

		coll.insert(doc);

		JSONObject response = new JSONObject();
		try {
			response.put("ack", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}

	@GET
	@Path("/getcontext/{ebayId}/deviceid/{deviceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getContext(@PathParam("ebayId") String ebayId,
			@PathParam("deviceId") String deviceId,
			@Context HttpServletResponse responseContext) throws UnknownHostException {
		MongoClient mongoClient2 = new MongoClient("localhost" , 27017);
		DB db = mongoClient2.getDB("whoosh");
		DBCollection coll = db.getCollection("whooshcontext");

		// get Data
		/*
		 * { "ebayid" : "MongoDB", "deviceid" : "database", "desc" : 1
		 * 
		 * }
		 */
		// find all where i > 50

		BasicDBObject query = new BasicDBObject("ebayId", ebayId).append("targetDeviceId", deviceId).append("sent", "false").append("later", "false");
		//BasicDBObject query = new BasicDBObject("ebayId", ebayId).append("targetDeviceId", deviceId);
		DBCursor cursor = coll.find(query).sort( new BasicDBObject("time", -1));
		JSONObject context = new JSONObject();
		int count = 0;
		try {
			while (cursor.hasNext() && count < 1) {
				count++;
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				context = new JSONObject();
				
				context.put("ebayId", ebayId);
				context.put("targetDeviceId", dbObj.getString("targetDeviceId"));
				context.put("finalURL", dbObj.getString("finalURL"));
				context.put("pageId", dbObj.getString("pageId"));
				context.put("baseURL", dbObj.getString("baseURL"));
				context.put("name", dbObj.getString("name"));
				context.put("popup", dbObj.getString("popup"));
				String contextString = dbObj.getString("context");
				JSONObject contextObj = new JSONObject(contextString);
				context.put("context", contextObj);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		JSONObject response = new JSONObject();
		try {
			response.put("ack", "success");
			response.put("whooshContext", context);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Before returning, set the "sent" field to true, so that it will not be sent again.
		if (count > 0) { 
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", new BasicDBObject().append("sent", "true"));
			coll.update(query, updateQuery);
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		mongoClient2.close();
		return response.toString();
	}
	
	@GET
	@Path("/getsavedcontext/{ebayId}/deviceid/{deviceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSavedContext(@PathParam("ebayId") String ebayId,
			@PathParam("deviceId") String deviceId,
			@Context HttpServletResponse responseContext) {
		MongoClient mongoClient = MongoDBHelper.getInstance().getMongoClient();
		DB db = mongoClient.getDB("whoosh");
		DBCollection coll = db.getCollection("whooshcontext");

		// get Data
		/*
		 * { "ebayid" : "MongoDB", "deviceid" : "database", "desc" : 1
		 * 
		 * }
		 */
		// find all where i > 50

		BasicDBObject query = new BasicDBObject("ebayId", ebayId).append("targetDeviceId", deviceId).append("later", "true");
		DBCursor cursor = coll.find(query).sort( new BasicDBObject("time", -1));
		JSONArray contextArray = new JSONArray();
		JSONObject context = new JSONObject();
		int count = 0;
		try {
			while (cursor.hasNext()) {
				count++;
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				context = new JSONObject();
				
				context.put("ebayId", ebayId);
				context.put("targetDeviceId", dbObj.getString("targetDeviceId"));
				context.put("finalURL", dbObj.getString("finalURL"));
				context.put("pageId", dbObj.getString("pageId"));
				context.put("baseURL", dbObj.getString("baseURL"));
				context.put("name", dbObj.getString("name"));
				String contextString = dbObj.getString("context");
				if (contextString != null && !contextString.isEmpty()) {
					JSONObject contextObj = new JSONObject(contextString);
					if (contextObj != null) {
						context.put("context", contextObj);
					}
				}
				contextArray.put(context);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		JSONObject response = new JSONObject();
		try {
			response.put("ack", "success");
			response.put("whooshContext", contextArray);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		
		// Before returning, set the "sent" field to true, so that it will not be sent again.
		if (count > 0) { 
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", new BasicDBObject().append("sent", "true"));
			coll.update(query, updateQuery);
		}

		responseContext.setHeader("Access-Control-Allow-Origin", "*");
		return response.toString();
	}
	
	/* ebay hackathon - color sensor */
	@GET
	@Path("/setcolor/{red}/{green}/{blue}/{clear}")
	@Produces(MediaType.APPLICATION_JSON)
	public String setcolor(@PathParam("red")  String red,
			@PathParam("green") String green, @PathParam("blue") String blue,
			@PathParam("clear") String clear) {
		// get the xy color.
		getRGBtoXY(Integer.parseInt(red), Integer.parseInt(green), 
				Integer.parseInt(blue), Integer.parseInt(clear));
		
		// make PUT call to HUE
		
		return HUE_X + " : " + HUE_Y;
	}

	private static void getRGBtoXY(int r, int g,
							int b, int clear) {
	    // For the hue bulb the corners of the triangle are:
	    // -Red: 0.675, 0.322
	    // -Green: 0.4091, 0.518
	    // -Blue: 0.167, 0.04
	    double[] normalizedToOne = new double[3];

	    normalizedToOne[0] =  (float) ((r*1.0/clear) * 255);
	    normalizedToOne[1] =  (float) ((g*1.0/clear) * 255);
	    normalizedToOne[2] =  (float) ((b*1.0/clear) * 255);
	    float red, green, blue;

	    // Make red more vivid
	    if (normalizedToOne[0] > 0.04045) {
	        red = (float) Math.pow(
	                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
	    } else {
	        red = (float) (normalizedToOne[0] / 12.92);
	    }

	    // Make green more vivid
	    if (normalizedToOne[1] > 0.04045) {
	        green = (float) Math.pow((normalizedToOne[1] + 0.055)
	                / (1.0 + 0.055), 2.4);
	    } else {
	        green = (float) (normalizedToOne[1] / 12.92);
	    }

	    // Make blue more vivid
	    if (normalizedToOne[2] > 0.04045) {
	        blue = (float) Math.pow((normalizedToOne[2] + 0.055)
	                / (1.0 + 0.055), 2.4);
	    } else {
	        blue = (float) (normalizedToOne[2] / 12.92);
	    }

	    float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
	    float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
	    float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

	    float x = X / (X + Y + Z);
	    float y = Y / (X + Y + Z);

	    HUE_X = x;
	    HUE_Y = y;
	    System.out.println("X " + HUE_X + " Y " + HUE_Y);
	}

	/* ebay hackathon - whoosh */
	@SuppressWarnings("deprecation")
	@GET
	@Path("/createDraft/{title}/{price}")
	@Produces(MediaType.APPLICATION_JSON)
	public String createDraft(@PathParam("title") String title,
			@PathParam("price") String price) throws Exception {
		// Desktop.getDesktop().browse(new URI("http://www.google.com"));
		Map<String, String> params = new HashMap<String, String>();
		params.put("listingMode", "AddItem");
		params.put("startPrice", price);
		params.put("title", title);

		executeSOAPCall("createListingDraft", params);

		return title + "-" + price;
	}

	private static SOAPMessage createSOAPRequest(String serviceName,
			Map<String, String> params) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.setPrefix("soapenv");
		envelope.addNamespaceDeclaration("ser", SERVERURI);

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		soapBody.setPrefix("soapenv");
		setSOAPBody(soapBody, serviceName, params);

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("X-EBAY-SOA-OPERATION-NAME", serviceName);
		setSOAPHeaders(headers);

		soapMessage.saveChanges();

		/* Print the request message */
		System.out.print("Request SOAP Message:");
		soapMessage.writeTo(System.out);
		System.out.println();

		return soapMessage;
	}

	public static SOAPMessage executeSOAPCall(String serviceName,
			Map<String, String> params) throws Exception {

		// Create SOAP Connection
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory
				.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory
				.createConnection();

		// Send SOAP Message to SOAP Server

		SOAPMessage soapResponse = soapConnection.call(
				createSOAPRequest(serviceName, params), ENDPOINT);

		// print SOAP Response
		System.out.print("Response SOAP Message:");
		soapResponse.writeTo(System.out);

		soapConnection.close();

		return soapResponse;
	}

	private static void setSOAPHeaders(MimeHeaders headers) {
		headers.addHeader("X-EBAY-SOA-SECURITY-APPNAME", "WebNext");
		headers.addHeader("X-EBAY-SOA-SERVICE-VERSION", "1.0.0");
		headers.addHeader(
				"X-EBAY-SOA-SECURITY-TOKEN",
				"AgAAAA**AQAAAA**aAAAAA**tpwGUw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AFlIuhD5WCoQqdj6x9nY+seQ**u9wAAA**AAMAAA**THZfZEQBu4p9IcpXnUU8JodRXcqsZZE6z3zgQKmgslBSpvMjYbfC7Srum3ISgEUoekysPsAL9HcMHBR8zJ100uCoafts4751HIjf5mx22uGOVmZN9mXVcc1Bk7moobHKPy6VdnpT/ivcubdVuLX4sAg6HADADdG6o2ZkJdKsLy24/OO95XuBjg5PZecl0Y8OD9jtGwnL0kJSqN5Sd+ooaNowhE8JuBWtSDb8L50ikx2Ar0Lv8CgRvFYA4w1kwDL8ntkGXOsPM3tFNRSm4pP1ZcefAo0PkzHvMyuTa/VpjxJ/D0AQnhdg39QkupfSxovRu0fcZhHv/DXmVNVPOoFfk63v150LnCKrRT0gBvKLO2X0K9xedHGPQanYN0NaBonHVUK3zBeMHOdrzqIt8+vLMTRVPJJsuX1QAA6vR4LstvC3GBv5CRt3RVMSjNAek/hP3rLcOGZmnQjNzN1tDbrGsFnDUBSXcVmekUGu2kxuEjPM7Cj5QQ7LvVrXyDwdhDK8wv0se2YR84Yr0TFAQd9M7IstQGGfJBxKuz7P2cO/wSYThGgg7ClzSbvtX4yOfybGv6rb2fFQBPFp9sBlwyI+miqCaIYOoY8Fzmc0owYEepvUXXqRZgGLH7vZWcMVtFmZXy7cnmckDrh2w5LRS0w8JuyB6l2ZPN2raw7kOx3k0xOP6hbEiMmQhCfZOnd3Hhj1UxjxlbHWhIzo5G+rjz98tVaLnEg+TQ3QuAwlIsroTaw8BQji8ibCVNp+sCcGU5p4");
		headers.addHeader("X-EBAY-SOA-SERVICE-NAME", "ListingDraftService");
		headers.addHeader("X-EBAY-SOA-GLOBAL-ID", "EBAY-US");
	}

	private static void setSOAPBody(SOAPBody soapBody, String serviceName,
			Map<String, String> params) throws SOAPException {
		SOAPElement soapBodyElem = soapBody.addChildElement(serviceName
				+ "Request", "ser");
		SOAPElement soapBodyTemp, soapBodyTemp2, soapBodyTemp3, soapBodyTemp4;
		String aux;

		switch (serviceMap.get(serviceName)) {

		case createListingDraft:
			soapBodyTemp2 = soapBodyElem
					.addChildElement("draftInitInfo", "ser");
			soapBodyTemp = soapBodyTemp2.addChildElement("listingMode", "ser");
			aux = params.get("listingMode");
			soapBodyTemp.addTextNode(aux);

			soapBodyTemp3 = soapBodyElem.addChildElement("listing", "ser");
			soapBodyTemp = soapBodyTemp3.addChildElement("startPrice", "ser");
			aux = params.get("startPrice");
			soapBodyTemp.addTextNode(aux);

			soapBodyTemp4 = soapBodyTemp3.addChildElement("Item", "ser");
			soapBodyTemp = soapBodyTemp4.addChildElement("title", "ser");
			aux = params.get("title");
			soapBodyTemp.addTextNode(aux);

			soapBodyTemp2 = soapBodyElem.addChildElement("directive", "ser");
			soapBodyTemp = soapBodyTemp2.addChildElement("name", "ser");
			aux = "SAVE_DRAFT_FOR_LATER";
			soapBodyTemp.addTextNode(aux);

			soapBodyTemp = soapBodyTemp2.addChildElement("action", "ser");
			aux = "COMPLETE";
			soapBodyTemp.addTextNode(aux);

		}
	}

}
