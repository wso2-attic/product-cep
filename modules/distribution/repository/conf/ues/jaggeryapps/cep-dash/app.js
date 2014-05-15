var configs = require('/config.json');


var store = require('store');
store.server.init(configs);

store.user.init(configs);

var app = require('/modules/app.js');
app.init(configs);