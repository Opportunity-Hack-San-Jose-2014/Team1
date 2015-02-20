exports.addRoutes = function(app) {
	app.get('/', require('./src/ui-pages/home'));
	app.get('/home', require('./src/ui-pages/home'));
    app.get('/search', require('./src/ui-pages/search'));
    app.get('/view', require('./src/ui-pages/view'));
};