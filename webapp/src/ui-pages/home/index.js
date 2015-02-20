var express = require('express');
var app = express();

var templatePath = require.resolve('./template.marko');
var template = require('marko').load(templatePath);

app.use(function(req, res, next){
	template.render({
	        name: 'Home page'
	    }, res);
});

module.exports = app;