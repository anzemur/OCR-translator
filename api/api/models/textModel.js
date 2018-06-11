'use strict';

var mongoose = require('mongoose');

var TextSchema = new mongoose.Schema({
    translated_text: {
        type: String
    }
});

module.exports = mongoose.model('TextModel', TextSchema);
