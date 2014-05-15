/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This script is part of the UES js library
 */
var ues = ues || {};
ues.gadgets = ues.gadgets || {};
ues.gadgets.core = ues.gadgets.core || {};

(function () {
    var GADGET_RENDERED_CLASS = 'gadget-rendered';

    //public method to enabling gadget Drag and Drop in a page
    ues.gadgets.core.enableDnD = function () {
        //holds the gadget layout in a page
        ues.gadgets.core.gadgetLayout = ues.gadgets.core.gadgetLayout || {};

        var gadgetAreaElm = $(".gadgetArea");
        gadgetAreaElm.sortable({
            connectWith: ".gadgetArea",
            stop: function (event, ui) {

                $(".gadgetArea").each(function () {
                    var gadgetIds = $(this).sortable('toArray');
                    for (var i = 0; i < gadgetIds.length; i++) {
                        var gadgetId = gadgetIds[i];
                        var gadgetProps = ues.util.getGadgetProps(gadgetId);
                        deleteGadgetFromLayout(gadgetId);

                        ues.gadgets.core.gadgetLayout[$(this).attr('id')][gadgetId] = gadgetProps;
                    }
                });
                console.log(ues.gadgets.core.gadgetLayout);
//                $.post('/portal/lib/gadget-rendering/server/layout.jag', {'gadgetLayout': JSON.stringify(ues.gadgets.core.gadgetLayout)});
            }
        });

        //gadgetAreaElm.disableSelection();
    };

    var deleteGadgetFromLayout = function (id) {
        for (var gadgetAreaId in ues.gadgets.core.gadgetLayout) {
            var gadgetArea = ues.gadgets.core.gadgetLayout[gadgetAreaId];
            for (var gadgetId in gadgetArea) {
                if (gadgetId == id) {
                    delete ues.gadgets.core.gadgetLayout[gadgetAreaId][gadgetId];
                }
            }
        }

    };

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

    //public function to draw gadgets
    ues.gadgets.core.drawGadgets = function () {
        //gadgetLayout has areas and corresponding gadgets
        for (var areaId in ues.gadgets.core.gadgetLayout) {
            //taking the gadgets object for an area
            var gadgets = ues.gadgets.core.gadgetLayout[areaId];

            for (var gadgetId in gadgets) {
                //a gadget from a list of gadgets
                var gadget = gadgets[gadgetId];

                //Asynchronously building the gadgets
                setTimeout((function (gadgetUrl, gadgetId, areaId) {
                    return function () {
                        ues.gadgets.gadgetContainer.preloadGadget(gadgetUrl, function (result) {
                            var divForGadget =  getDivForGadget(gadgetId, areaId);
                            console.log(gadgetId, areaId,divForGadget);

                            if (divForGadget.length>0 && !divForGadget.hasClass(GADGET_RENDERED_CLASS)) {
                                //adding preloaded info to the container object
                                ues.gadgets.gadgetContainer.preloadedGadgetUrls_[gadgetUrl] = result;
                                //building the gadget frame
                                buildGadgetFrame(gadgetId, areaId);

                                //build gadget's userPrefs pane
                                buildGadgetHeaderPane(result, gadgetUrl, gadgetId);

                                //building the gadget content
                                ues.gadgets.core.buildGadget(result, gadgetUrl, gadgetId, areaId);

                                ues.gadgets.events.register(gadgetId);
                            }
                        });
                    };
                }(gadget.url, gadgetId, areaId)), 0);
                // }
            }
        }
    };

    //private function to build the gadget
    ues.gadgets.core.buildGadget = function (result, gadgetURL, curId, areaId) {

        //creating a new element to hold the gadget
        var element = $('#' + areaId).children('.gadget').children('#gadget-content-' + curId).get([0]);

        //parsing the element to get the gadget
        $(element).data('gadgetSite', ues.gadgets.gadgetContainer.renderGadget(gadgetURL, curId, element, ues.util.getGadgetProps(curId)));

        //determine which button was click and handle the appropriate event.
        $('.portlet-header .ui-icon').click(function () {
            handleNavigateAction($(this).closest('.portlet'), $(this).closest('.portlet').find('.portlet-content').data('gadgetSite'), gadgetURL, this.id);
        });
    };

    var buildGadgetFrame = function (curId, areaId) {
        //getting templates from ues.addons
        var gHeader = ues.gadgets.addons.gadgetParts.header(curId);
        var gSetting = ues.gadgets.addons.gadgetParts.settings(curId);
        var gContent = ues.gadgets.addons.gadgetParts.gadgetBox(curId);

        //appending to an outer div aka gadget container
        var divForGadget = getDivForGadget(curId, areaId);
        $($($('<div/>', {
            'class': 'gadget',
            'attr': {},
            'id': curId
        }).append(gHeader)).append(gSetting)).append(gContent).appendTo(divForGadget);

        divForGadget.addClass(GADGET_RENDERED_CLASS);

    };

    //returns the jquery div object the gadget should be rendered into.
    var getDivForGadget = function (curId, areaId) {
        return $('#' + areaId);
    };

    //creates the gadget's settings pane
    var buildGadgetHeaderPane = function (result, gadgetUrl, curId) {
        result[gadgetUrl] = result[gadgetUrl] || {};

        //@TODO: handle localPrefs
        var savedPrefs = ues.util.getGadgetProps(curId).userPrefs || {};

        if (!result[gadgetUrl].error) {
            $('#gadget-header-' + curId).children('h2').text(result[gadgetUrl]['modulePrefs'].title);
            var gadgetnameArr = gadgetUrl.split("/");
            //console.log('gadget::' + gadgetnameArr[gadgetnameArr.length - 1]);
            var gadgetName = gadgetnameArr[gadgetnameArr.length - 1];
            $('#gadget-header-' + curId).attr('name', gadgetName);

            //asigning the default prefs as rendered prefs
            var renderedPrefs = result[gadgetUrl]['userPrefs'];


//            uncomment this to clip aria of the gadget.
//            if (result[gadgetUrl]['modulePrefs'].height) {
//                $('#gadget-content-' + curId).height(result[gadgetUrl]['modulePrefs'].height + 20);
//            }

            //hide "settings" for gadgets without userPrefs
            if (!$.isEmptyObject(renderedPrefs)) {
                $('#gadget-settings-dropdown-' + curId).append('<li><a class="btn-gadget-settings" href="#" data-gadget-id="' + curId + '">Settings</a></li>');
            }

            //If default prefs are overridden the renderedPrefs = savedPrefs
            if (Object.keys(savedPrefs).length != 0) {
                for (var key in renderedPrefs) {
                    var pref = renderedPrefs[key];
                    pref.defaultValue = savedPrefs[pref.name];
                    renderedPrefs[key] = pref;
                }
            }

            var gadgetForm = prefsInputForm(renderedPrefs, curId);
            $('#gadget-settings-' + curId).html(gadgetForm);

            //console.log(gadgetForm);
            /*if (gadgetForm != '<div id="formdiv-gadget-site-1"><form id="gadget-form-1"></form></div> ') {
             //$('#gadget-header-' + curId).append('<br>' + gadgetForm);
             $('#gadget-settings-' + curId).html(gadgetForm);
             for (var key in savedPrefs) {
             if (savedPrefs.hasOwnProperty(key)) {
             // console.log(key + " -uu-> " + userpref[key]);
             console.log(key + " ----uu-> " + savedPrefs[key]);
             if ($("#" + curId + key).attr('type') == "checkbox") {
             $("#" + curId + key).attr('checked', savedPrefs[key]);
             } else if ($("#" + curId + key).attr('type') == "text") {
             $("#" + curId + key).attr('value', savedPrefs[key]);
             } else {
             $('option:selected', 'select').removeAttr('selected')
             $("#" + curId + key).val("\"" + savedPrefs[key] + "\"");
             //  $('#0mycolor').val(2);
             console.log(key + " DOoo ----uu-> " + savedPrefs[key]);

             }
             }
             }
             }*/
        }
    };

    //creating userPrefs form according to data types
    var prefsInputForm = function (obj, id) {
        var size = 0, key;
        var out = '<div id="formdiv-gadget-site-' + id + '"> \
				      <form id="gadget-settings-form-' + id + '" class="settings-form form-horizontal">';

        for (key in obj) {
            if (obj.hasOwnProperty(key))
                size++;
            var x = obj[key];

            out += '<div class="control-group">';
            if (x.dataType == "STRING") {
                out += '<label class="control-label" for="' + x.displayName + '">' + x.displayName + '</label>' + '<div class="controls">' + "<input type=\"text\" name=\"" + x.name + "\" id=\"" + id + x.name + "\" value=\"" + x.defaultValue + "\">" + '</div>';
            } else if (x.dataType == "BOOL") {
                var chk = "checked";
                if (!x.defaultValue) {
                    var chk = "unchecked";
                }
                out += ' <div class="controls"><label class="checkbox">' + "<input type=\"checkbox\" name=\"" + x.name + "\" id=\"" + id + x.name + "\"value=\"" + x.defaultValue + "\" checked=\"" + chk + "\">" + x.displayName + '</label></div>'
            } else if (x.dataType == "ENUM") {
                out += x.displayName + "<select name=\"" + x.name + "\" id=\"" + id + x.name + "\">";
                for (var i = 0; i < x.orderedEnumValues.gadgets.core.length; i++) {
                    out += "<option value=\"" + x.orderedEnumValues[i].value + "\""
                    if (x.orderedEnumValues[i].value == x.defaultValue) {
                        out += "selected=\"true\" >";
                    } else {
                        out += ">";
                    }
                    out += x.orderedEnumValues[i].displayValue + "</option>"
                }
                out += '</select>';
            }
            out += '</div>';

        }
        out += '<div class="control-group">\
                <div class="controls">\
                    <input id="settings-cancel-' + id + '" type="button" class="btn settings-cancel" value="Cancel" />\
                    <input id="settings-save-' + id + '" type="button" class="btn settings-save" value="Save" />\
                </div>';
        out += '</form></div>'
        return out;
    };

})();
