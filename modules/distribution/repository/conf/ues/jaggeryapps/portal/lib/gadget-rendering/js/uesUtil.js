var ues = ues || {};
ues.util = ues.util || {};

(function () {
    //returns the prefs associated with a gadget
    ues.util.getGadgetProps = function (id) {
        for (var gadgetAreaId in ues.gadgets.core.gadgetLayout) {
            var gadgetArea = ues.gadgets.core.gadgetLayout[gadgetAreaId];
            for (var gadgetId in gadgetArea) {
                if (gadgetId == id) {
                    return(ues.gadgets.core.gadgetLayout[gadgetAreaId][gadgetId]);
                }
            }
        }
    };

    //return the gadget area id associated to a gadget
    ues.util.getGadgetAreaIdFromLayout = function (id) {
        for (var gadgetAreaId in ues.gadgets.core.gadgetLayout) {
            var gadgetArea = ues.gadgets.core.gadgetLayout[gadgetAreaId];
            for (var gadgetId in gadgetArea) {
                if (gadgetId == id) {
                    return gadgetAreaId;
                }
            }
        }


    };

    ues.util.getAreaIdFromDom = function (element) {
        var thisParent = $(element).parent();
        if(!thisParent.hasClass('gadgetArea')){
            return ues.util.getAreaIdFromDom(thisParent);
        }
        return $(thisParent).attr('id');
    };



}());