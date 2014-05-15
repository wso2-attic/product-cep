//GadgetID
var curId = 0;

var host;

var testGadgets = [];

var api;
var obj;
var indicators;

$(function() {

	//initializing the common container
	CommonContainer.init();
	host = resolveHost();

	// TODO: we need to integrate the REST api get the following gadgets, in milestone 2
	testGadgets = [host + '/portal/gadgets/intro-gadget-1/intro-gadget-1.xml', host + '/portal/gadgets/intro-gadget-2/intro-gadget-2.xml'];
	drawGadgets();
	setTimeout('updateIntroGadgets(1)', 2000);
});

var updateIntroGadgets = function(j) {

	j = parseInt(j);

	switch(j) {

		case 1:
			indicators = ['SP.POP.TOTL', 'FR.INR.RINR'];
			break;

		case 2:
			indicators = ['EN.ATM.GHGO.KT.CE', 'NY.GDP.MKTP.CD'];
			break;

		case 3:
			indicators = ['FR.INR.RINR', 'EG.USE.PCAP.KG.OE'];
			break;

	}

	for (var i = 0; i < 2; i++) {

		api = 'http://api.worldbank.org/countries/USA/indicators/' + indicators[i] + '?format=jsonP&date=1990:2000&prefix=?';

		$.getJSON(api, (function(i) {
			return function(data) {
				obj = data[1];

				CommonContainer.inlineClient.publish('org.uec.geo.intro' + (i + 1), obj);

			};
		})(i));
	}

}
var drawGadgets = function() {
	CommonContainer.preloadGadgets(testGadgets, function(result) {
		for (var gadgetURL in result) {
			if (!result[gadgetURL].error) {
				buildGadget(result, gadgetURL);
				curId++;
			}
		}
	});
};

var gadgetTemplate = '<div class="portlet">' + '<div id="gadget-site" class="portlet-content"></div>' + '</div>';

var buildGadget = function(result, gadgetURL) {
	result = result || {};
	var element = getNewGadgetElement(result, gadgetURL);
	$(element).data('gadgetSite', CommonContainer.renderGadget(gadgetURL, curId));

};

var getNewGadgetElement = function(result, gadgetURL) {
	result[gadgetURL] = result[gadgetURL] || {};

	var newGadgetSite = gadgetTemplate;
	newGadgetSite = newGadgetSite.replace(/(gadget-site)/g, '$1-' + curId);

	$(newGadgetSite).appendTo($('#gadgetAreaIntro-' + curId));

	return $('#gadget-site-' + curId).get([0]);
}
var resolveHost = function() {
	//http://<domain>:<port>/
	return document.location.protocol + "//" + document.location.host;

}

