OHack 2014 for San Jose Public Library

The project was developed during the OHack hackathon 2014 held at eBay. The idea was to demonstrate the possibilities of expressing San Jose Public Library data in innovative ways, through applications powered by APIs.
To demonstrate this, we built a web application that supported the following operations
-  Browse library collections relevant to user queries
-  Complete user queries through auto suggestion
-  Recommend related searches
-  Provide faceted search to refine the collections further

##### Service Interfaces:

###### Search for all items in library
<pre><code>URL: GET http://&lt;server-name&gt;/ohack2014/sjpl/v1/search?key=value&api_key=KEY
Supported keys: keyword, collection, location (comma separated longitude, latitude values), 
                year (hyphen separated range), page, limit
</code></pre>
                

###### Related search items for a keyword query (in progress. Exposed as a different endpoint since it's a diff datasource)
<pre><code>URL: GET http://&lt;server-name&gt;/ohack2014/sjpl/v1/relatedsearch?key=value&api_key=KEY
Supported keys: keyword
</code></pre>


###### Get all the collections
<pre><code>URL: GET http://&lt;server-name&gt;/ohack2014/sjpl/v1/collections?&api_key=KEY
</code></pre>

###### Get the item details with resource id
<pre><code>URL: GET http://&lt;server-name&gt;/ohack2014/sjpl/v1/items/{item_id}?&api_key=KEY
</code></pre>

###### Update the item details with resource id
<pre><code>URL: POST http://&lt;server-name&gt;/ohack2014/sjpl/v1/items/{item_id}?&api_key=KEY
Data: JSON string of key value pair
</code></pre>

###### Get the histogram of the items
<pre><code>URL: GET http://&lt;server-name&gt;/ohack2014/sjpl/v1/histogram?key=value&api_key=KEY
Supported keys: keyword, collection, location (comma separated longitude, latitude values), 
                year (hyphen separated range)
</code></pre>
