var express = require('express');
var app = express();

var templatePath = require.resolve('./template.marko');
var template = require('marko').load(templatePath);

app.use(function(req, res, next){

	var Client = require('node-rest-client').Client;
	client = new Client();

	var itemId = req.query.id;
	// direct way
	client.get("http://localhost:8080/ohack2014/spendaday/v1/item/"+itemId, function(data, response){
            // parsed response body as js object
            console.log(data);
            // raw response
            //console.log(response);
        	template.render(data, res);
        });
});

module.exports = app;
