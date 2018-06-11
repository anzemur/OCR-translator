'use strict';

var mongoose = require('mongoose');

var StatusSchema = new mongoose.Schema({
    status: {
        type: String
    }
});

module.exports = mongoose.model('Status', StatusSchema);
