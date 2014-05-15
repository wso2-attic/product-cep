var caramel = require('caramel');
require('/app.js');
var portal = require('/portal.js').config();
var sites = require('/sites/site.json');
var rxtPath = '/sites/', deployer = require('/modules/deployer.js'), 
context = caramel.configs().context, 
base = portal.server.http + context + rxtPath, 
log = new Log('portal.site.deployer');

var populateSites = function() {
	var slength = sites.sites.length;
	for( i = 0; i < slength; i++) {
		var name = sites.sites[i].name;

		var path = base + name + '/';
		deployer.site({
			name : sites.sites[i].name,
			tags : sites.sites[i].tags.split(','),
			rate: sites.sites[i].rate,
			provider : sites.sites[i].attributes.overview_provider,
			version : sites.sites[i].attributes.overview_version,
            description : sites.sites[i].attributes.overview_description,
			url : portal.server.http + sites.sites[i].attributes.overview_url,
			thumbnail : portal.server.http + sites.sites[i].attributes.images_thumbnail,
			banner : portal.server.http + sites.sites[i].attributes.images_banner,
			status : sites.sites[i].attributes.overview_status
		});
	}

	log.info("Default sites deployed");
};
populateSites();
