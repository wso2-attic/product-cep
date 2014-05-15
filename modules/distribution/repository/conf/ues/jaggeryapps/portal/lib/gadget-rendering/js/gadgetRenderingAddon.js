var ues = ues || {};
ues.gadgets = ues.gadgets || {};
ues.gadgets.addons = ues.gadgets.addons || {};

(function () {
    ues.gadgets.addons.gadgetParts = ues.gadgets.addons.gadgetParts || {};
    ues.gadgets.addons.gadgetParts.header = function (id) {


        return '<div> \
											<div id="gadget-header-' + id + '" class="gadget-header"> \
												<a class="show-options"></a> \
												<ul class="gadget-controls pull-right"> \
													<li> \
														<a id="settings-' + id + '" class="dropdown-toggle settings-cog" data-toggle="dropdown"><i class="icon-cog"></i></a> \
													</li> \
													<li> \
														<a id="minimize-' + id + '" class="gadget-minimize"><i class="icon-chevron-up"></i></a> \
													</li> \
													<li> \
														<a id="maximize-' + id + '" class="gadget-maximize" data-target="' + id + '"><i class="icon-resize-full"></i></a> \
													</li> \
												</ul> \
												<h2></h2> \
											</div>';

    };

    ues.gadgets.addons.gadgetParts.settings = function (id) {
        return '<div id="gadget-settings-' + id + '" class="gadget-settings" style=""> </div>'
    };

    ues.gadgets.addons.gadgetParts.gadgetBox = function (id) {
        return '<div id="gadget-content-' + id + '" class="gadget-content"> </div></div>'
    };

}());