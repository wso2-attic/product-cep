var gadgetRxtPath = '/gadgets/';

var repoPath = '/gadgets';
var relativeRepoPath = 'gadgets/';

var lastUpdated = 0;

var DEPLOYING_INTERVAL = 10000;

var caramel = require('caramel');
require('/app.js');

var portal = require('/portal.js').config();

var populate = function () {
    var i, name, length, gadgets, file, path, xml, location
        log = new Log('portal.deployer'),
        repo = new File(repoPath),
        deployer = require('/modules/deployer.js'),
        context = caramel.configs().context,
        base = portal.server.http + context + gadgetRxtPath;

    if (repo.isDirectory()) {
        gadgets = repo.listFiles();
        length = gadgets.length;
        for (i = 0; i < length; i++) {
            name = gadgets[i].getName();
            if (skipGadgets(name))
                continue;
            file = new File(repoPath + '/' + name + '/' + name + '.xml');
            if (file.getLastModified() > lastUpdated) {
                var existingSession = Session["started"];
                if (existingSession) {
                    log.info('Deploying Gadget : ' + name);
                }
                location = repoPath + '/' + name;
                path = base + name + '/';
                file.open('r');
                var fileContent = file.readAll();
                fileContent = fileContent.replace(/^\s*<\?.*\?>\s*/, "");
                xml = new XML(fileContent);
                file.close();
                deployer.gadget({
                    name: xml.*::ModulePrefs.@title,
                    tags: (String(xml.*::ModulePrefs.@tags)).split(','),
                    rate: Math.floor(Math.random() * 5) + 1,
                    provider: portal.user.username,
                    version: '1.0.0',
                    description: xml.*::ModulePrefs.@description,
                    url: name + '/' + name + '.xml',
                    thumbnail: path + 'thumbnail.jpg',
                    banner: path + 'banner.jpg',
                    status: 'CREATED',
                    dataformat: relativeRepoPath + name + '/datasource/data-format.json',
                    chartoptions: gadgetRxtPath + name + '/config/chart-options.json',
                    location: location

                });
            }
        }
        if (typeof(Session["started"]) == "undefined") {
            log.info('Default gadgets deployed');
        }
        Session["started"] = true;

    }
    lastUpdated = new Date().getTime();
};

var skipGadgets = function (name) {
    if (name === 'agricultural-land' ||
        name === 'wso2-carbon-dev' ||
        name === 'intro-gadget-1' ||
        name === 'intro-gadget-2' ||
        name === 'gadget-template-explorer' ||
        name === 'gadget-explorer' ||
        name === 'co2-emission' ||
        name === 'electric-power' ||
        name === 'energy-use' ||
        name === 'greenhouse-gas') return true;
};

var addSSOConfig = function () {
    deployer = require('/modules/deployer.js');
    //Adding SSO Configs
    deployer.sso({'issuer': 'store',
        'consumerUrl': portal.ssoConfiguration.storeAcs,
        'doSign': 'true',
        'singleLogout': 'true',
        'useFQUsername': 'true',
        'issuer64': 'c3RvcmU'});

    deployer.sso({'issuer': 'portal',
        'consumerUrl': portal.ssoConfiguration.portalAcs,
        'doSign': 'true',
        'singleLogout': 'true',
        'useFQUsername': 'true',
        'issuer64': 'cG9ydGFs'});
};

var logPortalUrl = function () {
	var log = new Log();
    log.info("UES Portal URL : " + portal.server.http + caramel.configs().context);
};

populate();
//addSSOConfig();
setInterval(function () {
    //TEMP fix for task not clearing properly during server shutdown
    try {
        populate();
    } catch (e) {
    }
}, DEPLOYING_INTERVAL);
setTimeout(logPortalUrl, 5000);
