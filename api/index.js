'use strict';
var express = require('express');
var bP = require('body-parser');

var routes = require('./api/routes/translateRoutes');
var text = require('./api/models/textModel');
var status = require('./api/models/statusModel');


var app = express();

var server = app.listen(process.env.PORT || 3000, function () {
    var host = server.address().address;
    host = (host === '::' ? 'localhost' : host);
    var port = server.address().port;

    console.log('Listening at: http://%s:%s', host, port);

});


app.use(bP.urlencoded({ extended: true }));
app.use(bP.json());

// to allow CORS
app.all('/*', function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "X-Requested-With");
  res.header("Access-Control-Allow-Headers", "Content-Type");
  next();
});


routes(app);



/** Error handling **/

app.use(function(req, res) {
  res.status(404).send({error: 'Url not found!', url: req.originalUrl})

});
