'use strict';

var express = require('express');


module.exports = function(app) {
    var translateController = require('../controllers/translateStringController');

    var api_router = express.Router();
    var version_router = express.Router();

    app.use('/api', api_router);
    api_router.use('/v1', version_router);



    version_router.route('/translate')
        .post(translateController.translateString);


};
