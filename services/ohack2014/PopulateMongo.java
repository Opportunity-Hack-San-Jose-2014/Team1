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

public class PopulateMongo {

	public static void main(String[] args) {
		MongoClient client;
		try {
			client = new MongoClient("localhost" , 27017);
			readFile("mongo.txt", client);
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
    	//String sellers[] = {"rich.s", "easyshopping", "fantasticdeals", "canon", "besybuy"};
    	String sellers[] = {"peach_photo", "bh-photo", "canon", "greatdeals", "cheap-camera-deals"};
    	String format[] = {"Auction", "Buy It Now"};
    	DB db = client.getDB("ebay-hackathon");
    	DBCollection coll = db.getCollection("listings");
    	int count = 0;
    	
    	try {
        	String image = br.readLine();
        	while (image != null) {
        		titles = new BufferedReader(new FileReader("title.txt"));
        		String title = titles.readLine();
        		while (title != null) {
        			count++;
        			BasicDBObject doc = new BasicDBObject("title", title).
                            append("price", 250 + Math.abs(rand.nextInt() % 50) ).
                            append("image", image).
                            append("format", format[Math.abs(rand.nextInt() % 2)]).
                            append("seller", sellers[Math.abs(rand.nextInt() % 5)]);
        			coll.insert(doc); 
        			title = titles.readLine();
        		}
        		image = br.readLine();
        	}
        	System.out.print("Done " + count);
    	} finally {
    		br.close();
    	}
    }
	
	

}
