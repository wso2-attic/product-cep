var ues = ues || {};
ues.gadgets = ues.gadgets || {};
ues.gadgets.events = ues.gadgets.events || {};

(function () {
    ues.gadgets.events.register = function (id) {
        expandSettings(id);
        saveSettings(id);
        collapseSettings(id);
        removeGadget(id);
        maximizeGadget(id);
        minimizeGadget(id);
    };

    var expandSettings = function (id) {
        $('#settings-' + id).click(function () {
            $('#gadget-settings-' + id).show();
        });
    };

    var saveSettings = function (id) {
        $('#settings-save-' + id).click(function () {
            var propsArr = $('#gadget-settings-form-' + id).serializeArray();

            var gadgetAreaId = ues.util.getGadgetAreaIdFromLayout(id);
            for (var prop in propsArr) {
                var thisProp = propsArr[prop];
                ues.gadgets.core.gadgetLayout[gadgetAreaId][id].userPrefs[thisProp.name] = thisProp.value;
            }

            $('#gadget-settings-' + id).hide();

//            $.post('/portal/lib/gadget-rendering/server/layout.jag', {'gadgetLayout': JSON.stringify(ues.gadgets.core.gadgetLayout)});

            ues.gadgets.core.buildGadget({}, ues.gadgets.core.gadgetLayout[gadgetAreaId][id].url, id, gadgetAreaId);

        });
    };

    var collapseSettings = function (id) {
        $('#settings-cancel-' + id).click(function () {
            $('#gadget-settings-' + id).hide();
        });
    };

    var removeGadget = function (id) {
        $('#remove-' + id).click(function () {
            var thisAreaId = ues.util.getAreaIdFromDom(this);
            delete ues.gadgets.core.gadgetLayout[thisAreaId][id];
//            $.post('/portal/lib/gadget-rendering/server/layout.jag', {'gadgetLayout': JSON.stringify(ues.gadgets.core.gadgetLayout)});
            $('#' + id).hide();
        });
    };

    var maximizeGadget = function (id) {
        $('#maximize-' + id).click(function () {

        });
    };

    var minimizeGadget = function (id) {
        $('#minimize-' + id).click(function () {
            if ($('#minimize-' + id + ' > i').hasClass('icon-chevron-up')) {
                $('#gadget-content-' + id).hide();
                $('#minimize-' + id + ' > i').removeClass("icon-chevron-up").addClass("icon-chevron-down");
            } else {
                $('#gadget-content-' + id).show();
                $('#minimize-' + id + ' > i').removeClass("icon-chevron-down").addClass("icon-chevron-up");
            }
        });
    };

}());