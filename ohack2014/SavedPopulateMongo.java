package com.stories.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class SavedPopulateMongo {

	public static void main(String[] args) {
		MongoClient client;
		try {
			client = new MongoClient("localhost" , 27017);
			readFile("keywords-mongo.txt", client);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void readFile(String fileName, MongoClient client) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
    	BufferedReader titles = new BufferedReader(new FileReader("title.txt"));
    	String[] parts, keywords = null;
    	Random rand = new Random(); 
    	
    	DB db = client.getDB("ebay-hackathon");
    	DBCollection coll = db.getCollection("images");
    	ArrayList keywordList = new ArrayList();
    	
    	try {
        	String line = br.readLine();
        	while (line != null) {
        		if (line.contains("keyword")) {
        			parts = line.split(":");
        			keywords = parts[1].split(" ");
        			System.out.println(parts[1]);
        		} else {
        			keywordList.clear();
        			for (String kw: keywords) {
        			    keywordList.add(kw);
        			}
        			BasicDBObject doc = new BasicDBObject("keywords", keywordList).
                            append("price", 5 + Math.abs(rand.nextInt() % 20) * 5).
                            append("image", line);
        			coll.insert(doc);     
        			System.out.println(line);
        		}
        		line = br.readLine();
        	}
    	} finally {
    		br.close();
    	}
    }
	
	

}
