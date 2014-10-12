OHack 2014 for San Jose Public Library


Publicly Available REST APIs
Currently the APIs are open to everybody. You do not need an api_key


1. Search for all items in library

URL: GET http://10.225.93.250:8080/ohack2014/sjpl/v1/search?key=value&api_key=KEY
Supported keys: keyword, collection, location (comma separated longitude, latitude values), 
                year (hyphen separated range), page, limit
                

2. Related search items for a keyword query
URL: GET http://10.225.93.250:8080/ohack2014/sjpl/v1/relatedsearch?key=value&api_key=KEY
Supported keys: keyword


3. Get all the collections
URL: GET http://10.225.93.250:8080/ohack2014/sjpl/v1/collections?&api_key=KEY


4. Get the item details with resource id
URL: GET http://10.225.93.250:8080/ohack2014/sjpl/v1/items/{item_id}?&api_key=KEY


5. Update the item details with reource id
URL: POST http://10.225.93.250:8080/ohack2014/sjpl/v1/items/{item_id}?&api_key=KEY
Data: JSON string of key value pair

6. Get the histogram of the items
URL: POST http://10.225.93.250:8080/ohack2014/sjpl/v1/histogram?key=value&api_key=KEY
Supported keys: keyword, collection, location (comma separated longitude, latitude values), 
                year (hyphen separated range)
