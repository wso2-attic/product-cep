var caramel = require('caramel');


var carbon = require('carbon');
var conf = carbon.server.loadConfig('carbon.xml');
var offset = conf.*::['Ports'].*::['Offset'].text();
var hostName = conf.*::['HostName'].text().toString();

if (hostName === null || hostName === '') {
    hostName = 'localhost';
}

var httpPort = 9763 + parseInt(offset, 10);
var httpsPort = 9443 + parseInt(offset, 10);

var process = require('process');
process.setProperty('server.host', hostName);
process.setProperty('http.port', httpPort.toString());
process.setProperty('https.port', httpsPort.toString());


caramel.configs({
    context: '/portal',
    negotiation: true,
    themer: function () {
        return 'portal';
    }
});

var configs = require('/portal.js').config();
var portal = require('/modules/portal.js');

configs.login = portal.login;
configs.logout = portal.logout;
configs.register = portal.register;

var mod = require('store');
mod.server.init(configs);

mod.user.init(configs);

portal.init(configs);
