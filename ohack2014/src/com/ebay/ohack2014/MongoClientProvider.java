package com.ebay.ohack2014;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;


public class MongoClientProvider {
	private static MongoClient mongoClient;
	
	static {
		try {
			mongoClient = new MongoClient("localhost" , 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static MongoClient getMongoClient() {
		return mongoClient;
	}
}