$(function () {

    var dashboardName = $('#dashboardName').val();
    var tab = 'Profile';

    // TODO: get the default dash/first occurance if dashboard name isnt specified
    if (dashboardName == "") {
        dashboardName = "Default";
    }

    $.ajax({
        url: '/portal/apis/portal/dashboard.jag?action=listgadgets&dashboardname=' + dashboardName + '&tab=' + tab + '&gadgetarea=main',
        dataType: 'json',
        success: function (data) {
            console.log(data.error);
            if (data.error) {
                $('.preloader').hide();
                $('.tab-content').html('<div class="dashboard-error">  ' + data.message + ' \
				<div id="add-gadget" class="gadget img-rounded span4"> \
				<a class="btn-add-gadget" data-dashboard="' + dashboardName + '" data-tab="Profile" href="/portal/store.jag?dashboard=' + dashboardName + '&tab=Profile"> \
				<img src="/portal/themes/default/img/icon-gadget-add-new.png"> <span> Add new Gadget here</span> </a> \
				</div> \
				</div>');
            } else {
                $.each(data.gadgets, function () {
                    //console.log(this.gadget.url.toString());
                    console.log("user pref: " + this.gadget.userprefs);
                    window.buildGadgetTemplet(curId, this.gadget.uuid.toString());
                    window.buildGadget(this.gadget.url.toString(), curId, this.gadget.userprefs);
                    curId++;

                });

                if (curId) {
                    //alert("curId:" + curId);
                    $.updateIframes();
                }

            }


        }
    });
    var curId = 0;
    var updatedData = "";

    //create a gadget with navigation tool bar header enabling gadget collapse, expand, remove, navigate to view actions.
    window.buildGadget = function (gadgetURL, id, localUserprefs) {

        $('#gadget-' + id).attr('data-gadgeturl', gadgetURL);
        var elem = document.getElementById('gadget-content-' + id);
        var gadget = gadgetURL;
        var container = new osapi.container.Container();
        var site = container.newGadgetSite(elem);
        var renderParams = { view: 'home', width: 400, height: 200, nocache: "true", userPrefs: localUserprefs };

        container.preloadGadget(gadget, function (result) {


            if (!result[gadget].error) {
                container.navigateGadget(site, gadget, {}, renderParams);
                $('#gadget-header-' + id).children('h2').text(result[gadget]['modulePrefs'].title);
                var gadgetnameArr = gadget.split("/");
                console.log('gadget::' + gadgetnameArr[gadgetnameArr.length - 1]);
                var gadgetName = gadgetnameArr[gadgetnameArr.length - 1];
                $('#gadget-header-' + id).attr('name', gadgetName);
                var obj = result[gadget]['userPrefs'];


//                uncomment this to clip aria of the gadget.
//                if (result[gadget]['modulePrefs'].height) {
//                    $('#gadget-content-' + id).height(result[gadget]['modulePrefs'].height + 20);
//                }

                //hide "settings" for gadgets without userPrefs
                if (!$.isEmptyObject(obj)) {
                    $('#gadget-settings-dropdown-' + id).append('<li><a class="btn-gadget-settings" href="#" data-gadget-id="' + id + '">Settings</a></li>');
                }

                var gadgetForm = Object.gadgetForm(obj, id);
                //console.log(gadgetForm);
                if (gadgetForm != '<div id="formdiv-gadget-site-1"><form id="gadget-form-1"></form></div> ') {
                    //$('#gadget-header-' + id).append('<br>' + gadgetForm);
                    $('#gadget-settings-' + id).html(gadgetForm);
                    for (var key in localUserprefs) {
                        if (localUserprefs.hasOwnProperty(key)) {
                            // console.log(key + " -uu-> " + userpref[key]);
                            console.log(key + " ----uu-> " + localUserprefs[key]);
                            if ($("#" + id + key).attr('type') == "checkbox") {
                                $("#" + id + key).attr('checked', localUserprefs[key]);
                            } else if ($("#" + id + key).attr('type') == "text") {
                                $("#" + id + key).attr('value', localUserprefs[key]);
                            } else {
                                $('option:selected', 'select').removeAttr('selected')
                                $("#" + id + key).val("\"" + localUserprefs[key] + "\"");
                                //  $('#0mycolor').val(2);
                                console.log(key + " DOoo ----uu-> " + localUserprefs[key]);

                            }
                        }
                    }
                }


                //updateIframes();

                /*
                 setTimeout(function(){

                 },1000);*/


            }
        });
    };

    refreshGadget = function (gadgetURL, id, userpref) {

        var elem = document.getElementById('gadget-content-' + id);
        var container = new osapi.container.Container();
        var site = container.newGadgetSite(elem);
        var gadget = gadgetURL;
        var userPrefsObject = userpref;
        var renderParams = { view: 'home', width: 400, height: 200, userPrefs: userPrefsObject };

        container.preloadGadget(gadget, function (result) {


            if (!result[gadget].error) {
                //	container.closeGadget(site);
                container.navigateGadget(site, gadget, {}, renderParams);
                var obj = result[gadget]['userPrefs'];
                var gadgetForm = Object.gadgetForm(obj, id);
                $('#gadget-settings .modal-body').html('');
                $("#" + id + "gadget").attr('value', 'madtest');
                for (var key in userpref) {
                    if (userpref.hasOwnProperty(key)) {
                        // console.log(key + " -uu-> " + userpref[key]);
                        console.log(key + " ----uu-> " + userpref[key]);
                        if ($("#" + id + key).attr('type') == "checkbox") {
                            $("#" + id + key).attr('checked', userpref[key]);
                        } else if ($("#" + id + key).attr('type') == "text") {
                            $("#" + id + key).attr('value', userpref[key]);
                        } else {
                            $('option:selected', 'select').removeAttr('selected')
                            $("#" + id + key).val("\"" + userpref[key] + "\"");
                            //  $('#0mycolor').val(2);
                            console.log(key + " DOoo ----uu-> " + userpref[key]);

                        }

                    }
                }
                /*
                 setTimeout(function(){
                 updateIframes();
                 },3000);*/

            }
        });
    };

    window.buildGadgetTemplet = function (id, uuid) {


        var gadgetHeader = '<div> \
											<div id="gadget-header-' + id + '" class="gadget-header"> \
												<a class="show-options"><img src="themes/default/img/icon-gadget-three-dots.png"></a> \
												<ul class="gadget-controls pull-right"> \
													<li class="dropdown"> \
														<a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i></a> \
														<ul id="gadget-settings-dropdown-' + id + '" class="dropdown-menu pull-right"> \
															<li> \
																<a href="#">Duplicate this Gadget</a> \
															</li> \
															<li> \
																<a href="#">Copy to</a> \
															</li> \
															<li> \
																<a href="#">About this Gadget</a> \
															</li> \
														</ul> \
													</li> \
													<li> \
														<a class="gadget-minimize" href="#"><i class="icon-chevron-up"></i></a> \
													</li> \
													<li> \
														<a class="gadget-maximize" data-target="' + id + '" href="#"><i class="icon-resize-full"></i></a> \
													</li> \
													<li> \
														<a href="#"><i class="icon-remove"></i></a> \
													</li> \
												</ul> \
												<h2></h2> \
											</div>';


        var gadgetSettings = '<div id="gadget-settings-' + id + '" class="gadget-settings" style="display: none"> </div>';
        /*
         <form class="form-horizontal"> \
         <div class="input-prepend"> \
         <span class="add-on">Background Color</span> \
         <input class="span2" id="prependedInput" type="text" value="#FFFFFF"> \
         </div> \
         <div class="input-prepend"> \
         <span class="add-on">Border size</span> \
         <input class="span2" id="prependedInput" type="text" value="0"> \
         </div> \
         <div class="input-prepend"> \
         <span class="add-on">Number of Entries</span> \
         <input class="span2" id="prependedInput" type="text" value="5"> \
         </div> \
         <button class="btn btn-small btn-primary"> \
         Save Settings \
         </button> \
         </form> \*/


        var gadgetContent = '<div id="gadget-content-' + id + '" class="gadget-content"> </div></div>';

        if (typeof(id) === 'number') {
            var gadgetli = document.createElement('li');
            gadgetli.setAttribute('class', 'gadget img-rounded span4');
            gadgetli.setAttribute('data-row', 1);
            gadgetli.setAttribute('data-col', 1);
            gadgetli.setAttribute('data-sizex', 1);
            gadgetli.setAttribute('data-sizey', 10);
            gadgetli.setAttribute('id', 'gadget-' + id);
            gadgetli.setAttribute('gadgetuuid', uuid);
            gadgetli.innerHTML = gadgetHeader + gadgetSettings + gadgetContent;
            var elem = document.getElementById("profile-ul");
            elem.appendChild(gadgetli);
        }


    };


    Object.gadgetForm = function (obj, id) {
        var size = 0, key;
        var out = '<div id="formdiv-gadget-site-' + id + '"> \
				      <form id="gadget-form-' + id + '">';

        for (key in obj) {
            if (obj.hasOwnProperty(key))
                size++;
            //console.log(size + key);
            var x = obj[key];

            /*
             console.log(x);
             console.log(x.dataType);
             console.log(x.defaultValue);
             console.log(x.displayName);
             console.log(x.name);*/


            if (x.dataType == "STRING") {
                out += x.displayName + ": <input type=\"text\" name=\"" + x.name + "\" id=\"" + id + x.name + "\" value=\"" + x.defaultValue + "\"><br>";
            } else if (x.dataType == "BOOL") {
                var chk = "checked";
                if (!x.defaultValue) {
                    var chk = "unchecked";
                }
                out += "<input type=\"checkbox\" name=\"" + x.name + "\" id=\"" + id + x.name + "\"value=\"" + x.defaultValue + "\" checked=\"" + chk + "\">" + x.displayName + "<br>"
            } else if (x.dataType == "ENUM") {
                //console.log(x.orderedEnumValues.length)
                out += x.displayName + "<select name=\"" + x.name + "\" id=\"" + id + x.name + "\">";
                for (var i = 0; i < x.orderedEnumValues.length; i++) {
                    //	console.log(x.orderedEnumValues[i]);
                    out += "<option value=\"" + x.orderedEnumValues[i].value + "\""
                    //	console.log(x.orderedEnumValues[i].value + " : " + x.defaultValue);
                    //	console.log(x.orderedEnumValues[i].value == x.defaultValue);
                    if (x.orderedEnumValues[i].value == x.defaultValue) {
                        out += "selected=\"true\" >";
                    } else {
                        out += ">";
                    }
                    out += x.orderedEnumValues[i].displayValue + "</option>"
                }
                out += '</select>';


            }
        }
        out += '</form></div>'
        //	console.log(out);
        return out;
    };


    $.fn.serializeFormJSON = function () {

        var o = {};
        var a = this.serializeArray();
        $.each(a, function () {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };


    $(".btn-gadget-settings").live('click', function () {

        var gadgetId = $(this).attr('data-gadget-id');
        var gadgetSettings = $('#gadget-settings-' + gadgetId);

        $('#btn-save-userpref').attr({
            'data-gadgetId': gadgetId,
            'data-form': this,
            'data-loading-text': 'Loading...'
        });

        $('#gadget-settings .modal-body').html(gadgetSettings.html());

        $('#gadget-settings').modal(show = true, backdrop = false);

    });

    $("#remove-dashboard").live('click', function () {


        $('#span-remove-dash').text(dashboardName);

        $('#remove-dash-modal').modal(show = true, backdrop = false);

    });


    $('#btn-remove-dash').click(function () {
        $.ajax({
            url: '/portal/apis/portal/dashboard.jag?action=removedashboard&dashboardname=' + dashboardName,
            dataType: 'json',
            success: function (data) {
                if (data.error) {
                    alert(data.message);
                } else {
                    alert(dashboardName + " was removed successfully");
                    window.location.href = '/portal/';
                }
            }
        });
    });


    $('#btn-save-userpref').click(function () {
        var gadgetId = $(this).attr('data-gadgetid');
        var form = $(this).data('form');
        Dashboard.saveUserPrefs(form, gadgetId);
        $('#gadget-settings').modal("hide");
    });

    $('#add-new-dashboard').click(function () {
        $('#add-new-dashboard-modal').modal(show = true, backdrop = false);
    });


    $('.nav-item').click(function () {
        $('#menu-dashboards > li').removeClass('active');
        $(this).parents('li').addClass('active');
        var dashboard = $(this).attr('data-target');
        $.ajax({
            type: 'POST',
            url: '/portal/dashboard.jag',
            data: {'dashboard': dashboard},
            success: function () {
                window.location.href = '/portal/dashboard.jag';
            }
        })
    });

    $('.gadget-settings-close').on('click', function () {
        $(this).parents('.gadget-settings').slideToggle("fast");
    });

    $('#save-new-dashboard').click(function () {
        var dashboardname = $('#new-dashboard-name').val();

        $.ajax({
            type: 'POST',
            url: '/portal/apis/portal/dashboard.jag',
            dataType: 'json',
            data: {action: 'adddashboard', dashboardname: dashboardname},
            success: function (result) {
                if (!result.error) {

                    alert("New empty dashboard created successfully");
                    $('#add-new-dashboard-modal').modal('hide');

                    if ($('#menu-dashboards > li').not('.dropdown').length < 5) {
                        $('#menu-dashboards .dropdown').before('<li><a href="?dashboard=' + dashboardname + '">' + dashboardname + '</a></li>');
                    } else {
                        $('#menu-dashboards .dropdown-menu').prepend('<li><a href="#">' + dashboardname + '</a></li>')
                    }

                } else {
                    alert(result.message);
                }

            }

        });
    });


    /*
     $('.btn-add-gadget').live('click', function(){
     var dashboard = $(this).attr('data-dashboard');
     var tab = $(this).attr('data-tab');

     $.ajax({
     type: 'POST',
     url: '/portal/store.jag',
     data: {'dashboard': dashboard, 'tab':tab},
     success: function(){
     window.location.href = '/portal/store.jag';
     }
     })

     });

     */
    // define gridster's its own namespace inside jQuery object
    $.gridster = {
        positions: [],
        full_width: false
    };

    var loaded = 0;

    var responsivegadget = function () {
        var idx = 1;
        var sizey = 0;

        if ($('.span12').width() <= 724 && $.gridster.full_width === false) {// when scaled down
            $.gridster.positions.length = 0;
            // clear previous positions
            $.gridster.full_width = true;
            $('.gridster > ul >li').each(function () {
                var row = $(this).attr('data-row');
                var col = $(this).attr('data-col');
                var prevSizey = parseInt($(this).prev().attr('data-sizey'));

                (prevSizey) ? sizey += prevSizey : 0;

                $.gridster.positions.push({
                    "row": row,
                    "col": col
                });
                $(this).attr({
                    'data-col': '1',
                    'data-row': sizey + 1
                });
                idx++;
            });

        } else if ($('.span12').width() > 724 && $.gridster.full_width === true) {// when scaled up
            $.gridster.full_width = false;
            $('.gridster > ul >li').each(function () {
                $(this).attr({
                    'data-col': $.gridster.positions[idx - 1].col,
                    'data-row': $.gridster.positions[idx - 1].row
                });
                idx++;
            });

        }
    }


    $('.gadgets').css("opacity", 0);

    $.updateIframes = function () {


        loaded = 0;

        // TODO: find a solution to avoid this timeout

        setTimeout(function () {
            var iframes = $('.gadget-content > iframe');

            //alert(iframes.length);

            iframes.each(function () {
                console.log("iframe length " + iframes.length);
                //alert("Iframe ID: " + $(this).width());
                //$(this).bind('load', function() {

                $.browser.chrome = /chrome/.test(navigator.userAgent.toLowerCase());

                if ($.browser.chrome) {
                    //this.style.height = 'auto';
                }

                var wc = $(this).parent(".gadget-content");

                //var ifrHeight = this.contentWindow.document.body.scrollHeight;
                var D = this.contentWindow.document;

                $(this, top.document).contents().find("body").css('height', 'auto');


                //var D = document.getElementById("__gadget_gadget-content-0").contentWindow.document;
                var ifrHeight = Math.max(Math.max(D.body.scrollHeight, D.documentElement.scrollHeight), Math.max(D.body.offsetHeight, D.documentElement.offsetHeight), Math.max(D.body.clientHeight, D.documentElement.clientHeight));
                var settings = wc.siblings('.gadget-settings');
                var settingsHeight = settings.is(':visible') ? settings.height() : 0;

                var parentGadget = wc.parents("li.gadget");

                //if(parentGadget.attr('data-sizey') != '2') {
                parentGadget.attr("data-sizey", Math.round(ifrHeight / 22));
                parentGadget.attr("data-orig-sizey", Math.round(ifrHeight / 22));
                wc.height(parseInt(ifrHeight));
                //	} else {
                //	parentGadget.attr("data-sizey", 2);

                //}
                //setTimeout(function() {

                //console.log(ifrHeight);
                //}, 1000);

                loaded++;

                console.log("Loaded: " + loaded + " Total frames: " + iframes.length);

                if (loaded == iframes.length) {
                    console.log("all loaded");

                    $(".gridster > ul").gridster({
                        widget_margins: [10, 10],
                        widget_base_dimensions: [370, 10] // give min height = 1 to have correct snapping
                    });

                    //setTimeout(function() {
                    $('.preloader').animate({
                        opacity: 0
                    }, 1000).hide();
                    $('.gadgets').animate({
                        opacity: 1
                    }, 1000);

                    //}, 0);

                }
                //});

            });
        }, 1000);

    }

    $(window).bind('resize', responsivegadget);
    $(document).bind('ready', responsivegadget);

    $('#close-sticky-position').click(function () {
        $('#sticky-position').slideUp();
    });

    if ($('#sticky-position').is(':visible')) {
        var addHere = '<li class="gadget img-rounded span4" data-row="1" data-col="2" data-sizex="1" data-sizey="7" id="gadget-add-here"> \
								<div> \
									<i class="icon-move icon-large"></i><span> Drag this to position</span> \
								</div> \
							</li>';
        $('.gridster > ul').prepend(addHere);
        $.updateIframes();

    }

    $('#confirm-position').click(function () {
        var pos = $('#gadget-add-here');
        var row = pos.attr('data-row');
        var col = pos.attr('data-col');
        alert("Row: " + row + ", Col:" + col);
        $('#sticky-position').slideUp("slow", function () {
            // TODO: replace dummy gadget with real gadget
            setTimeout(function () {
                $('#black-overlay').fadeOut(2000);
            }, 200);

        });
    });

    $('.gadget-minimize').live('click', function () {

        var parent = $(this).parents('.gadget');
        var id = parent.attr('id');
        var row = parent.attr('data-sizey');

        $(this).children('i').attr('class', 'icon-chevron-down');
        $(this).attr('class', 'gadget-restore');

        var gridster_api = $(".gridster > ul").gridster().data('gridster');

        gridster_api.resize_widget($('#' + id), 1, 3);

        parent.height(55);


    });

    $('.icon-remove').live('click', function () {
        var parent = $(this).parents('.gadget');
        var id = parent.attr('id');

        var uuid = parent.attr('gadgetuuid');
        //portal/dashboard.jag?action=removegadget&page=page&gadgetarea=main&dashboardname=default&gadget=b45d8161-16d4-4bc5-8476-33a9996b67de
        console.log(uuid + "Click close gadget on " + id);
        //	var contentx = '{"addition":\''+addition+'\'}';
        var content = '{"action": \'removegadget\',"page":\'page\',"gadgetarea":\'main\',"dashboardname":\'default\',"gadget":\'' + uuid + '\'}';
        DashboardUtil.makeRequest("POST", "/portal/apis/portal/dashboard.jag?action=removegadget&page=page&gadgetarea=main&dashboardname=default&tab=Profile&gadget=" + uuid, null,

            function (html) {
                if (html.error) {
                    console.log("error" + html.message);
                    alert(html.message);
                } else {
                    console.log(id + " removed " + html.message);


                    var gridster_api = $(".gridster > ul").gridster().data('gridster');

                    gridster_api.remove_widget($('#' + id));
                }

            });
    });

    $('.gadget-restore').live('click', function () {

        var parent = $(this).parents('.gadget');
        var id = parent.attr('id');
        var origSizeY = parseInt(parent.attr('data-orig-sizey'));

        parent.removeAttr('style');
        parent.children('.gadget-header').height(35);
        var gridster_api = $(".gridster > ul").gridster().data('gridster');

        gridster_api.resize_widget($('#' + id), 1, origSizeY);

        $(this).children('i').attr('class', 'icon-chevron-up');
        $(this).attr('class', 'gadget-minimize');

    });

    $('.gadget-maximize').live('click', function () {
        var id = $(this).data('target');
        console.log(id);
        //var content = $('#gadget-content-'+id+' > iframe', top.document).contents().find("body").html();

        var li = $(this).parents('.gadget');

        var uuid = li.attr('gadgetuuid');
        var url = li.attr('data-gadgeturl');

        window.buildGadgetTemplet('modal', uuid);
        window.buildGadget(url, 'modal', null);

        var height = parseInt($('#gadget-content-' + id).css('height'));

        $('#gadget-maximize-modal').css('height', height + 100);
        $('#gadget-maximize-modal').children('.modal-body').css({'padding': 0, 'overflow': 'visible'});
        $('#gadget-maximize-modal').modal(show = true, backdrop = false);
        /*
         $('#gadget-maximize-modal').on('shown', function(){
         $('#gadget-content-modal').children('iframe').width("100%");
         });*/


    });


});