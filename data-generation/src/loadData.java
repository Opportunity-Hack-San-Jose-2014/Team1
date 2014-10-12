import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.cedarsoftware.util.io.JsonWriter;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;


public class loadData {

	public static List<String> LOCATIONS = new ArrayList<String>();
	public static List<String> IMAGE_URLS_HALL = new ArrayList<String>();
	public static List<String> IMAGE_URLS_SCHOOL = new ArrayList<String>();
	public static List<String> IMAGE_URLS = new ArrayList<String>();
	public static Random randomGeneratorLocations = new Random();
	public static Random randomGeneratorImageURLS = new Random();
	public static Random randomGeneratorYear = new Random();
	public static Random randomGeneratorLan = new Random();
	public static Random randomGeneratorLon = new Random();
	public static Random randomGeneratorSign = new Random();
	public static int total_titles = 0;
	public static int total_count = 0;

	public static void main(String[] args) throws Exception{

		loadLocations();
		loadImageURLs();
		String[] digitalCollections = {"fiesta-de-las-rosas","frontier-villiage","historic-map-and-atlas","historic-photograph"};
		for (String collectionName : digitalCollections)
			loadCollection(collectionName);
		MongoClient mongoClient = new MongoClient( "localhost" );
		DB db = mongoClient.getDB("itemdb");
		DBCollection coll = db.getCollection("items_details_collection_final5");
		DBCursor cursor = coll.find();
		String serialize = JSON.serialize(cursor);
		System.out.println(JsonWriter.formatJson(serialize));
		System.out.println("Total valid titles = " + total_titles);
	}

	private static void loadImageURLs() throws Exception {
		String imageURLStr =  loadFileAsString("image_urls");
		String [] imageURLs = imageURLStr.split("\\r?\\n");
		for(String imageURL:imageURLs)
			IMAGE_URLS.add(imageURL);
		String imageURLStrHall =  loadFileAsString("image_urls_hall");
		String [] imageURLsHall = imageURLStrHall.split("\\r?\\n");
		for(String imageURLHall:imageURLsHall)
			IMAGE_URLS_HALL.add(imageURLHall);	
		String imageURLStrSchool =  loadFileAsString("image_urls_school");
		String [] imageURLsSchool = imageURLStrSchool.split("\\r?\\n");
		for(String imageURLSchool:imageURLsSchool)
			IMAGE_URLS_SCHOOL.add(imageURLSchool);	
	}

	private static void loadLocations() throws Exception {
		String locationStr =  loadFileAsString("locations");
		String [] locations = locationStr.split("\\r?\\n");
		for(String location:locations)
			LOCATIONS.add(location);		
	}

	public static void loadCollection(String collectionName) throws Exception {
		String TEST_XML_STRING = loadFileAsString(collectionName);
		MongoClient mongoClient = new MongoClient( "localhost" );
		DB db = mongoClient.getDB("itemdb");
		DBCollection coll = db.getCollection("items_details_collection_final5");
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
			JSONObject rdfJSONObj = (JSONObject) xmlJSONObj.get("rdf:RDF");
			JSONArray descriptionJSONArray = (JSONArray) rdfJSONObj.get("rdf:Description");
			for (int i = 0, size = descriptionJSONArray.length(); i < size; i++)
			{
				total_count++;
				ItemModel entry = new ItemModel();
				JSONObject objectInArray = descriptionJSONArray.getJSONObject(i);
				String[] elementNames = JSONObject.getNames(objectInArray);
				for (String elementName : elementNames)
				{
					String value = objectInArray.getString(elementName);
					if(elementName.contains("type"))
						entry.type = value;
					else if(elementName.contains("subject"))
						entry.subject = value;
					else if(elementName.contains("creator"))
						entry.creator = value;
					else if(elementName.contains("about"))
						entry.about = value;
					else if(elementName.contains("source"))
						entry.source = value;
					else if(elementName.contains("contributor"))
						entry.contributor = value;
					else if(elementName.contains("description"))
						entry.description = value;
					else if(elementName.contains("relation"))
						entry.relation = value;
					else if(elementName.contains("rights"))
						entry.rights = value;
					else if(elementName.contains("publisher"))
						entry.publisher = value;
					else if(elementName.contains("format"))
						entry.format = value;
					else if(elementName.contains("date"))
						entry.date = getDate(entry,value);
					else if(elementName.contains("identifier"))
						entry.identifier = value;
					else if(elementName.contains("coverage"))
						entry.coverage = value;
					else if(elementName.contains("language"))
						entry.language = value;
					else if(elementName.contains("title")){
						if(value == null || value.isEmpty())
							entry.title = value;
						else
						{
							total_titles++;
							entry.title = value;
							System.out.println(value);
							String[] tokens = value.split("\\s+");
							entry.tags = tokens;
						}
					}
				}
				entry.id = total_count;
				entry.collection = collectionName;
				setLocation(entry);
				setImageSource(entry);
				ObjectMapper mapper = new ObjectMapper();
				String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry);
				DBObject dbObject = (DBObject)JSON.parse(jsonString);
				coll.insert(dbObject);
			}

		} catch (JSONException je) {
			System.out.println(je.toString());
		}
	}

	private static int getDate(ItemModel entry, String value) {
		int yearInt = randomGeneratorYear.nextInt(100);
		yearInt+=1900;
		return yearInt;
	}

	private static void setImageSource(ItemModel entry) {
		if(entry.title.toLowerCase().contains("hall"))
			entry.image_url = IMAGE_URLS_HALL.get(randomGeneratorImageURLS.nextInt(IMAGE_URLS_HALL.size()));
		else if (entry.title.toLowerCase().contains("school"))
			entry.image_url = IMAGE_URLS_SCHOOL.get(randomGeneratorImageURLS.nextInt(IMAGE_URLS_SCHOOL.size()));
		else
			entry.image_url = IMAGE_URLS.get(randomGeneratorImageURLS.nextInt(IMAGE_URLS.size()));
	}

	private static void setLocation(ItemModel entry) {
		String location = LOCATIONS.get(randomGeneratorLocations.nextInt(LOCATIONS.size()));
		String[] tokens = location.split(";");
		entry.location = tokens[0];
		float lat = Float.parseFloat(tokens[1]);
		int latpercent = randomGeneratorLan.nextInt(10);
		latpercent/=10000;
		if(randomGeneratorSign.nextBoolean() ==true)
			lat = (lat + lat*latpercent);
		else
			lat = (lat-lat*latpercent);
		entry.lat = Float.valueOf(lat).toString();

		float lon = Float.parseFloat(tokens[2]);
		int lonpercent = randomGeneratorLan.nextInt(10);
		lonpercent/=10000;
		if(randomGeneratorSign.nextBoolean() ==true)
			lon = (lon + lon*lonpercent);
		else
			lon = (lon-lon*lonpercent);
		entry.lon = Float.valueOf(lon).toString();
	}

	private static String loadFileAsString(String collectionName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("data/" + collectionName + ".txt"));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
}
