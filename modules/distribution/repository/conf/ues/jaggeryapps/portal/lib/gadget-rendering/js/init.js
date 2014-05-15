$(function () {

//following is the gadget layout object
/*    ues.gadgets.core.gadgetLayout = {
        'gadgetArea-1': {'0': {
            'url': 'http://www.gstatic.com/ig/modules/datetime_v3/datetime_v3.xml',
            'userPrefs': {},
            'frame': true
        }, '1': {
            'url': 'http://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/hamster.xml',
            'userPrefs': {},
            'frame': true
        }},
        'gadgetArea-2': {'2': {
            'url': 'http://nuwanbando.com/ig/soa.xml',
            'userPrefs': {},
            'frame': true
        }}
    };*/

    //list with gadget id, url pair

    //$.get('/portal/lib/gadget-rendering/server/layout.jag', function (data) {
        ues.gadgets.core.gadgetLayout = __gadgetLayout;
        ues.gadgets.gadgetContainer.init();
        ues.gadgets.core.drawGadgets();
        ues.gadgets.core.enableDnD();

    //});
});