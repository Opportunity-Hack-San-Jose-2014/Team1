var express = require('express');
var app = express();

//map routes to controller
require('./routes').addRoutes(app);
app.use('/static', express.static(__dirname + '/static'));
app.use('/public', express.static(__dirname + '/public'));

require('optimizer').configure('optimizer-config.json');

var server = app.listen(3000, function() {
    console.log('Listening on port %d', server.address().port);

    if (process.send) {
        process.send('online');
    }
});