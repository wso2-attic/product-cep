var caramel = caramel || {};
var layout, dummy_gadget_block = 50, block_params = {
    max_width: 6,
    max_height: 6
}, MARGINS_RATIO = 0.1, COLS = block_params.max_width, isPortal = (caramel.context == '/portal') ? true : false;

var onShowAssetLoad, tmpGadgetInfo, isDsChanged = false, isQueryChanged = false, isQueryRan = false, isGadgetChanged = false,
    firstTime = true;
var drawGadgets;

var flow_data = {};
var metadata;
var newWid = 0;

(function ($) {

    var extensions = {
        resize_widget_dimensions: function (options) {
            if (options.widget_margins) {
                this.options.widget_margins = options.widget_margins;
            }

            if (options.widget_base_dimensions) {
                this.options.widget_base_dimensions = options.widget_base_dimensions;
            }

            this.min_widget_width = (this.options.widget_margins[0] * 2) + this.options.widget_base_dimensions[0];
            this.min_widget_height = (this.options.widget_margins[1] * 2) + this.options.widget_base_dimensions[1];

            var serializedGrid = this.serialize();
            this.$widgets.each($.proxy(function (i, widget) {
                var $widget = $(widget);
                var data = serializedGrid[i];
                this.resize_widget($widget, data.sizex, data.sizey);
            }, this));

            this.generate_grid_and_stylesheet();
            this.get_widgets_from_DOM();
            this.set_dom_grid_height();
            return false;
        }
    };
    $.extend($.Gridster, extensions);
})(jQuery);

$.validator.addMethod("alphanumeric", function (value, element) {
    return this.optional(element) || /^[\w\-\s]+$/.test(value);
}, "Must be alphanumeric.");


$(function () {

    //TODO: Remove this snippet when other datasource support added
    var tempDisable = function(){
        $('[data-dstype="REST API"]').parent().removeClass("span4").addClass("span5");
        $('[data-dstype="REST API"]').parent().prop('disabled',true);
        $('[data-dstype="Cassandra"]').parent().removeClass("span4").addClass("span5");
        $('[data-dstype="Cassandra"]').parent().prop('disabled',true);
    }

    tempDisable();

    var $STORE_MODAL_TEMPLATE = $('#modal-add-gadget-wizard');
    var $STORE_MODAL_GADGET = $('#modal-add-gadget-existing');
    var $LAYOUTS_GRID = $('#layouts_grid');
    var newDimensions = calculateNewDimensions();

    var widgetId = 1;

    var widgetTemplate = Handlebars.compile($('#widget-template').html());
    var widgetTemplate2 = Handlebars.compile($('#widget-template2').html());

    function applyGridster() {
        var widgetId = 500;
        layout = $('.layouts_grid ul').gridster({
            widget_base_dimensions: newDimensions[0],
            widget_margins: newDimensions[1],

            serialize_params: function ($w, wgd) {
                var gadgetInfo = $($w.get(0)).data('gadgetInfo');
                var wclass = ($(wgd.el[0]).attr('class').indexOf('static') != -1) ? 'static' : '';
                var gadgetId = $w.find(".add-gadget-item > div").attr('id');
                var gadgetRenderInfo = UESContainer.getGadgetInfo(gadgetId);
                var prefs = gadgetRenderInfo && gadgetRenderInfo.opt.prefs || {};
                var currentWidgetId = $(wgd.el[0]).attr('data-wid');
                var url = $(wgd.el[0]).attr('data-url');
                return {
                    wid: currentWidgetId || widgetId++,
                    x: wgd.col,
                    y: wgd.row,
                    title: $w.find('input').val(),
                    width: wgd.size_x,
                    height: wgd.size_y,
                    prefs: JSON.stringify(prefs).replace(/"/g, "'"),
                    wclass: wclass,
                    url: gadgetInfo && (gadgetInfo.attributes.overview_url_temp || gadgetInfo.attributes.overview_url) || url
                };

            },
            //min_rows : block_params.max_height,
            max_cols: 6,
            max_size_x: 6
        }).data('gridster');
    }

    setTimeout(function () {
        drawGrid(newDimensions[0][0]);
    }, 2000);

    setGridOffsetTop();

    function calculateNewDimensions() {
        var containerWidth = $('#layouts_grid').innerWidth();
        var newMargin = containerWidth * MARGINS_RATIO / (COLS * 2);
        var newSize = containerWidth * (1 - MARGINS_RATIO) / COLS;
        return [
            [newSize, newSize],
            [newMargin, newMargin]
        ];
    }

    var timeOut;

    function resize() {
        var newDimensions = calculateNewDimensions();

        layout.resize_widget_dimensions({
            widget_base_dimensions: newDimensions[0],
            widget_margins: newDimensions[1]
        });

        drawGrid(newDimensions[0][0]);

        clearTimeout(timeOut);
        timeOut = setTimeout(setGridOffsetTop, 500);

    }

    function drawGrid(blockSize) {
        var h = $LAYOUTS_GRID.innerWidth() / blockSize;
        var v = $LAYOUTS_GRID.innerHeight() / blockSize;

        $('#grid-guides').html('').hide();

        for (var i = 0; i < v; i++) {
            for (var j = 0; j < h; j++) {

                var plus = '<i class="designer-guides-plus" data-row="' + (i + 1) + '" data-col="' + (j + 1) + '"></i>';
                $('#grid-guides').append(plus).fadeIn("slow");
            }
        }
    }

    var itemTmp = Handlebars.compile($('#item-template').html());

    $('#dummy-gadget').resizable({
        grid: dummy_gadget_block,
        containment: "#dummy-gadget-container",
        stop: function (event, ui) {
            var h = Math.round($(this).height()) / dummy_gadget_block;
            var w = Math.round($(this).width()) / dummy_gadget_block;
            var display = w + "x" + h;
            $(this).find('#dummy-size').html(display).attr({
                'data-w': w,
                'data-h': h
            });
        }
    });

    var registerEventsToWidget = function (widget) {
        var addGadgetBtn = $(widget).find('.btn-add-gadget-new, .btn-add-gadget-existing');
        addGadgetBtn.click(onGadgetSelectButton);
    };

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~start gadget-gen ui~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    function onGadgetSelectButton() {
        lastClickedGadgetButton = $(this);
        if (lastClickedGadgetButton.hasClass('btn-add-gadget-existing')) {
            $('#modal-add-gadget-existing').modal('show');
        } else if (lastClickedGadgetButton.hasClass('btn-add-gadget-new')) {
            $STORE_MODAL_TEMPLATE.modal('show');
        }

    }


    $STORE_MODAL_TEMPLATE.on('hidden', function () {
        flow_data = {};
        $("#wizard-add-gadget").steps('reset');
        $('#wizard-add-gadget > .steps > ul > li.done').removeClass('done').addClass('disabled');
        $('.wizard-dsType').removeClass('active');
        $('#wizard-dsTypeSel').val('');

        $('#gadgetArea-preview').html($("#gadgetPreviewPlaceholder").html());

        var cWindow = $('#store-gadget-div').find('iframe').get(0).contentWindow;
        cWindow.deselectGadget();

        $('#wizard-add-gadget-btn-prev').addClass('disabled');

    });

    $STORE_MODAL_GADGET.on('hidden', function () {
        var cWindow = $('#store-gadget-div2').find('iframe').get(0).contentWindow;
        cWindow.deselectGadget();
    });

    $("#wizard-add-gadget").steps({
        headerTag: "h3",
        bodyTag: "section",
        transitionEffect: "fade",
        onStepChanging: function (event, currentIndex, newIndex) {

            if (newIndex == 4) {
                $('#wizard-add-gadget-btn-next').hide();
                $('#wizard-add-gadget-btn-finish').show();
            } else {
                $('#wizard-add-gadget-btn-next').show();
                $('#wizard-add-gadget-btn-finish').hide();
            }

            switch (currentIndex) {
                case 0:
                    var dsType = $('#wizard-dsTypeSel').val();
                    if (flow_data.dataSource != dsType) {
                        flow_data.dataSource = dsType;
                        isDsChanged = true;
                    }
                    break;

                case 1:
                    var conSettings = {};
                    $('#wizard-add-gadget-p-1').find('.control-group').each(function () {
                        if (flow_data.dataSource == "RDBMS") {
                            conSettings[$(this).find('label').html()] = $(this).find(":selected").text();
                        } else {
                            conSettings[$(this).find('label').html()] = $(this).find('input').val();
                        }
                    });

                    flow_data.conSettings = conSettings;
                    isDsChanged = false;
                    break;

                case 2:
                    if (isQueryChanged) {
                        var queryData = {};
                        $('#wizard-add-gadget-p-2').find('.control-group').each(function () {
                            queryData[$(this).find('label').html()] = $(this).find('input').val() || $(this).find('textarea').val();
                        });

                        flow_data.appName = $('#inp-dashboard').val();
                        flow_data.queryData = queryData;
                        getQueryData();
                    }
                    break;

            }

            switch (newIndex) {
                case 1:
                    if (isDsChanged || !flow_data.conSettings) {
                        var nextWindowData;
                        var source;

                        if (flow_data.dataSource == "RDBMS") {
                            getDataSourceNames();
                            nextWindowData = {
                                dataSourceNames: flow_data.dataSourceNames
                            }
                            source = $("#create-new-connection-rdbms").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
                        } else {
                            nextWindowData = {
                                createConnection: metadata.dataSourcesDescriptions[flow_data.dataSource]
                            };
                            source = $("#create-new-connection-other").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
                        }
                        var template = Handlebars.compile(source);
                        $('#wizard-add-gadget-p-1').html(template(nextWindowData));
                    }
                    break;

                case 2:
                    if (!flow_data.queryData) {
                        var window3Data = metadata.datasourceWindow_3[flow_data.dataSource];
                        if (window3Data) {
                            var nextWindowData = {
                                sqlEditor: window3Data
                            };

                            var source = $("#sql-query-editor").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
                            var template = Handlebars.compile(source);
                            $('#wizard-add-gadget-p-2').html(template(nextWindowData));

                            $('.inp-query').change(function () {
                                isQueryChanged = true;
                                isQueryRan = false;
                                $('#sql-editor-dataset').empty();
                            });

                        } else {

                        }
                    }

                    break;

                case 4:
                    if (isGadgetChanged || isQueryRan) {
                        UESContainer.removeGadget('gadgetArea-preview');
                        $('#gadgetArea-preview').html($("#gadgetPreviewPlaceholder").html());
                        getDataFormat();
                    }
                    $('#time-series').attr('checked',false);
                    break;

            }

            return (currentIndex > newIndex) ? true : $('#form-wizard').valid({
                rules: {
                    chartTitle: {
                        alphanumeric: true
                    }
                }
            });
        }
    });

    //Next Click of 4th window (Gadget store window)
    onShowAssetLoad = function () {
        var cWindows = $('#store-gadget-div,#store-gadget-div2');

        cWindows.each(function (i) {
            var cw = $(this).find('iframe').get(0).contentWindow;
            if (cw.addListener) {
                cw.addListener(function (gadgetInfo) {
                    tmpGadgetInfo = gadgetInfo;
                    isGadgetChanged = true;
                });
            }
        });

    };

    var getDataSourceNames = function () {
        caramel.ajax({
            type: 'POST',
            url: "apis/gadgetGen?action=getDataSourceNames",
            data: JSON.stringify(flow_data),
            success: function (result) {
                flow_data.dataSourceNames = result.dataSourceNames;
            },
            async: false,
            contentType: 'application/json',
            dataType: 'json'
        });
        isQueryRan = true;
    }

    var getQueryData = function () {
        caramel.ajax({
            type: 'POST',
            url: "apis/gadgetGen?action=queryDbAll",
            data: JSON.stringify(flow_data),
            success: function (result) {
                flow_data.column_headers = result.tableHeaders;
            },
            async: false,
            contentType: 'application/json',
            dataType: 'json'
        });
        isQueryRan = true;
    }
    var getDataFormat = function () {
        isGadgetChanged = false;
        isQueryRan = false;
        $.ajax({
            type: 'POST',
            url: '/publisher' + tmpGadgetInfo.attributes.overview_dataformat,
            success: generateDataMapping,
            contentType: 'application/json',
            dataType: 'json'
        });
        //TODO: caramel.ajax prepends 'portal' as context
        /*
         caramel.ajax({
         type : 'POST',
         url : '/publisher' + tmpGadgetInfo.attributes.overview_dataformat,
         success : generateDataMapping,
         contentType : 'application/json',
         dataType : 'json'
         });
         */

    }
    var generateDataMapping = function (tableData) {
        var divCont = populateMappingRow(tableData.dataColumns, flow_data.column_headers, true);
        $('#modal-data-mapping').html(divCont);

        flow_data.dataColumns = tableData.dataColumns;

        var nextWindowData = {
            gadget_type: tmpGadgetInfo.attributes.overview_name,
            dataLabels: tableData.dataLabels
        };

        var nextWindowData_2 = {
            timeSeries : tableData.timeSeriesAllowed
        };

        var source = $("#data-mapping-extension").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        var template = Handlebars.compile(source);
        $('#modal-data-mapping-extension-space').html(template(nextWindowData));

        var source_2 = $("#time-series-extension").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        var template_2 = Handlebars.compile(source_2);
        $('#modal-time-series-extension-space').html(template_2(nextWindowData_2));

        if(firstTime){
        $('#mapping-add-series-btn').bind('click', addSeriesBtnClick);
        $('#mapping-remove-series-btn').bind('click', removeSeriesBtnClick);
        //To add more series
            firstTime = false;
        }

        $('#btn-preview-gadget').bind('click', function (e) {
            e.preventDefault();
            $('.gadget-preview-loader').fadeIn("fast");
            processFieldMapping('preview');
        });

    }
    var newlabelID = 0;

    var populateMappingRow = function (dataColumns, columnHeaders, isFirst) {
        var columns = [];
        if (!isFirst && dataColumns.length > 1) {
            var cloneDataColumns = dataColumns.slice(1);
            columns = cloneDataColumns;
        } else {
            columns = dataColumns;
        }
        var nextWindowData = {
            id: newlabelID++,
            dataColumns: columns,
            columnHeaders: columnHeaders
        };
        var source = $("#data-mapping").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        var template = Handlebars.compile(source);
        return template(nextWindowData);

    }
    var addSeriesBtnClick = function (e) {
        e.preventDefault();
        var divCont = populateMappingRow(flow_data.dataColumns, flow_data.column_headers, false);
        $('#modal-data-mapping').append(divCont);
    }
    var removeSeriesBtnClick = function (e) {
        e.preventDefault();
        if ($("#modal-data-mapping .form-horizontal").length > 1) {
            $("#modal-data-mapping .form-horizontal").last().remove();
        }
    }
    var processFieldMapping = function (mode) {
        var mappingData = [];
        var url = 'apis/gadgetGen?action=createJag';

        url = (mode == 'preview') ? url + '&mode=preview' : url;

        $('#modal-data-mapping').children().each(function () {
            var series = {}
            if (mappingData[0]) {
                series = jQuery.extend(true, {}, mappingData[0]);
            }
            var firstdiv = $(this).children().first();
            series[firstdiv.children()[0].innerHTML] = firstdiv.find('input').val();
            $(this).children().next().each(function () {
                series[$(this).children()[0].innerHTML] = $(this).find(":selected").text();
            });
            mappingData.push(series);
        });
        var dashboard = $('#inp-dashboard').val();
        flow_data.chartTitle = $('#chart-title-input').val();
        flow_data.refreshSequence = $('#refresh-sequence-input').val();
        flow_data.timeSeries = $('#time-series').is(":checked");

        var labelData = {};
        $('#data-labels').find('.control-group').each(function () {
            labelData[$(this).find('label').html()] = $(this).find('input').val();
        });

        flow_data.mappingData = mappingData;
        flow_data.dataLabels = labelData;
        flow_data.chartLocation = tmpGadgetInfo.attributes.overview_location;
        flow_data.chartType = tmpGadgetInfo.attributes.overview_name;
        flow_data.chartOptions = tmpGadgetInfo.attributes.overview_chartoptions;

        caramel.ajax({
            type: 'POST',
            url: url,
            data: JSON.stringify(flow_data),
            success: insertGadgetToTarget(mode),
            contentType: 'application/json',
            dataType: 'json'
        });

    }
    var insertGadgetToTarget = function (mode) {

        return function (data) {
            var modPrefs = {};
            var prefs = {};
            prefs.dataSource = data.jagPath;
            prefs.updateGraph = flow_data.refreshSequence;
            modPrefs.prefs = prefs;

            var gadgetLi;
            var tmpOverviewLoc = tmpGadgetInfo.attributes.overview_location;
            var tmpGadget = tmpOverviewLoc.substring(tmpOverviewLoc.lastIndexOf('/') + 1);
            tmpGadget += '/' + tmpGadget + ".xml";

            //$('#modal-data-mapper').modal('hide');

            if (mode == 'preview') {
                gadgetLi = $('#gadget-preview');
                gadgetLi.data('gadgetInfo', tmpGadgetInfo);
                insertGadgetPreview(gadgetLi, data.gadgetLocation + tmpGadget, modPrefs);
            } else {
                tmpGadgetInfo.attributes.overview_url_temp = data.gadgetLocation + tmpGadget;
                gadgetLi = lastClickedGadgetButton.parents('li');
                gadgetLi.data('gadgetInfo', tmpGadgetInfo);
                insertGadget(gadgetLi, tmpGadgetInfo.attributes.overview_url_temp, modPrefs, flow_data.chartTitle);
                lastClickedGadgetButton.closest('.gadget-add-btn-cont').remove();
                deleteTempFiles();
            }
        }
    }
    var deleteTempFiles = function () {
        tmpGadgetInfo = {};
        caramel.ajax({
            type: 'POST',
            url: 'apis/gadgetGen?action=deleteTemp',
            data: JSON.stringify(flow_data.appName),
            success: function () {
                $STORE_MODAL_TEMPLATE.modal('hide');
                flow_data = {};
            },
            contentType: 'application/json',
            dataType: 'json'
        });
    }

    $('#wizard-add-gadget-btn-prev').click(function () {
        $('a[href=#previous]').click();
    });
    $('#wizard-add-gadget-btn-next').click(function () {
        $('a[href=#next]').click();
    });
    $('#wizard-add-gadget-btn-finish').click(function () {
        processFieldMapping('dashboard');
    });

    $('#btn-add-gadget-existing').click(function () {
        var gadgetLi = lastClickedGadgetButton.parents('li');
        gadgetLi.data('gadgetInfo', tmpGadgetInfo);
        insertGadget(gadgetLi, tmpGadgetInfo.attributes.overview_url);
        lastClickedGadgetButton.closest('.gadget-add-btn-cont').remove();

        $STORE_MODAL_GADGET.modal('hide');
    });

    $('#wizard-add-gadget-btn-prev, #wizard-add-gadget-btn-next').bind('click', function () {
        var cssClass = $('.actions > ul > li').eq(0).attr('class');
        $('#wizard-add-gadget-btn-prev').removeClass().addClass('btn btn-primary btn-large ' + cssClass);

    })

    $('body').on('click', '.wizard-dsType', function (e) {
        //TODO : Change when other datasource type support added
        if($(this).attr('data-dsType') == 'RDBMS'){
            e.preventDefault();
            $('.wizard-dsType').removeClass('active');
            $(this).toggleClass('active');

            $('#wizard-dsTypeSel').val($(this).attr('data-dsType'));
        }
    });

    $('body').on('click', '.btn-validateCon', function (e) {
        e.preventDefault();
        var conSettings = {};
        $('#wizard-add-gadget-p-1').find('.control-group').each(function () {
            if (flow_data.dataSource == "RDBMS") {
                conSettings[$(this).find('label').html()] = $(this).find(":selected").text();
            } else {
                conSettings[$(this).find('label').html()] = $(this).find('input').val();
            }
        });

        flow_data.conSettings = conSettings;

        caramel.ajax({
            type: 'POST',
            url: "apis/gadgetGen?action=validateCon",
            data: JSON.stringify(flow_data),
            success: function (result) {
                alert(result.message);
            },
            contentType: 'application/json',
            dataType: 'json'
        });
    });

    $('body').on('click', '.btn-execQuery', function (e) {
        e.preventDefault();
        var queryData = {};
        $('#wizard-add-gadget-p-2').find('.control-group').each(function () {
            queryData[$(this).find('label').html()] = $(this).find('input').val() || $(this).find('textarea').val();
        });

        flow_data.appName = $('#inp-dashboard').val();
        flow_data.queryData = queryData;

        caramel.ajax({
            type: 'POST',
            url: "apis/gadgetGen?action=queryDbAll",
            data: JSON.stringify(flow_data),
            success: renderDatasetTable,
            contentType: 'application/json',
            dataType: 'json'
        });

        if (isQueryChanged) {
            isQueryRan = true;
        }
    });

    var renderDatasetTable = function (result) {

        $('#wizard-add-gadget-p-2 .well').animate({
            'margin-top': 0
        });

        flow_data.column_headers = result.tableHeaders;
        var source = $("#sql-query-table").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        var template = Handlebars.compile(source);
        $('#sql-editor-dataset').html(template(result));
    }
    //------------------------------------------------------------------------end of gadget-gen ui -------------------------------

    var eventRegistered = false;
    // is an event is resisted to show-asset gadget to get the selected gadget.

    drawGadgets = function () {
        applyGridster();

        if (!isPortal) {

            $.get('apis/ues/layout/', {},function (result) {
                if (result) {

                    var userWidgets = result.widgets;
                    var defaultWidgets = layout.serialize();

                    $.each(userWidgets, function (i, w) {

                        if (w.wid > newWid) {

                            newWid = w.wid;
                        }
                        //find w in defaultWidgets, if found copy attributes to _widget
                        if (isWidgetFound(w, defaultWidgets)) {

                            //update coords in default grid
                            $('.layout_block[data-wid="' + w.wid + '"]').attr({
                                'data-col': w.x,
                                'data-row': w.y,
                                'data-url': w.url,
                                'data-title': w.title,
                                'data-prefs': w.prefs
                            });

                        } else {
                            //add user widget to grid
                            layout.add_widget(widgetTemplate2({
                                wid: w.wid,
                                url: w.url,
                                prefs: w.prefs
                            }), w.width, w.height, w.x, w.y);
                        }
                    });

                    $.each(defaultWidgets, function (i, w) {
                        // skip static widgets
                        if (w.y == 1) {
                            return true;
                        }

                        if (w.wid > newWid) {
                            newWid = w.wid;
                        }

                        // remove widgets in default grid but not found in user widgets
                        if (!isWidgetFound(w, userWidgets)) {

                            var removeWidget = $('.layouts_grid').find('.layout_block[data-wid="' + w.wid + '"]');
                            layout.remove_widget($(removeWidget));
                        }
                    });

                    $('#dashboardName').find('span').text(result.title);

                }

                var widgets = $('.layouts_grid').find('.layout_block');

                $.each(widgets, function (i, widget) {
                    var $w = $(widget);
                    var wid = $w.attr('data-wid');
                    if (wid > newWid) {
                        newWid = wid;
                    }

                    var url = $w.attr('data-url');
                    var title = $w.attr('data-title');
                    var prefs = JSON.parse($w.attr('data-prefs').replace(/'/g, '"'));
                    var gadgetArea = $w.find('.add-gadget-item');
                    if (url != '') {
                        $w.find('.designer-placeholder').remove();
                        $w.find('.btn-add-gadget').remove();
                        insertGadget($w, url, {
                            prefs: prefs
                        }, title);
                    }
                    registerEventsToWidget($w);

                });

            }).error(function (error) {
                    console.log(error);
                });

            setGridOffsetTop();

            getDatasources();

            return;
        }
        var mode = $('#inp-view-mode').val(), layoutFormat, template, layoutType = $('#inp-layout').val();
        layoutFormat = (mode == 'view' || mode == '') ? $('.layout_block:not(.static)') : getLayoutFormat(layoutType);

        for (var i = 0; i < layoutFormat.length; i++) {
            template = itemTmp();
            var itemLayout = layoutFormat[i];
            var widget = layout.add_widget(template, itemLayout.width, itemLayout.height, itemLayout.x, itemLayout.y);
            registerEventsToWidget(widget);
        }
        setGridOffsetTop();
    }

    function getDatasources() {

        if (!metadata) {
            caramel.ajax({
                type: 'POST',
                url: "datasource-config.json",
                success: renderDatasources,
                contentType: 'application/json',
                dataType: 'json'
            });

        } else {
            genDataSourceDropdown(metadata);
        }
    }

    var renderDatasources = function (datasourceData) {
        metadata = datasourceData;
        var windowData = {
            dataSource: metadata.dataSources
        }

        var source = $("#select-data-source").html().replace(/\[\[/g, '{{').replace(/\]\]/g, '}}');
        var template = Handlebars.compile(source);
        $('#wizard-add-gadget-p-0').html(template(windowData));
    }

    function isWidgetFound(w, defaultWidgets) {
        var _widget = {};
        $.each(defaultWidgets, function (i, _w) {
            if (_w.wid == w.wid) {
                //_widget = {};
                _widget.x = _w.x;
                _widget.y = _w.y;
                return false;
            }
        });

        if (typeof _widget.x != 'undefined' || typeof _widget.y != 'undefined') {
            return true;
        }

        return false;

    }

    // check if default grid widget and user saved widget
    // have same positions. No change made if both are same
    // FROM personalization
    function isWidgetMatch(w1, w2) {

        var match = ((w1.x == w2.x) && (w1.y == w2.y));

        return match;
    }

    var lastClickedGadgetButton = null;

    //id to be use in dynamically added gadgets.
    var id = 1;

    function insertGadget(parentEl, url, pref, title) {
        id++;
        var gadgetDiv = parentEl.find('.add-gadget-item');
        var idStr = 'gadgetArea-d' + id;
        gadgetDiv.html('<div id="' + idStr + '">');

        if (isPortal) {
            UESContainer.renderGadget(idStr, url, pref || {}, function (gadgetInfo) {
                if (gadgetInfo.meta.modulePrefs) {
                    parentEl.find('.grid_header').append('<input class="gadget-title-txt" value="' + (title || gadgetInfo.meta.modulePrefs.title) + '">');
                    parentEl.find('.show-widget-pref').show();
                }
            });

        } else {
            UESContainer.renderGadget(idStr, url, pref || {}, function (gadgetInfo) {
                var visibleTitle = title || gadgetInfo.meta.modulePrefs.title;
                parentEl.find('h3').text(visibleTitle);
                parentEl.find('input').val(visibleTitle);

            });
            if (flow_data.mode == 'design') {
                parentEl.find('#header_label').hide();
                parentEl.find('#gadget_title_div').show();
            }
        }

    }

    function insertGadgetPreview(parentEl, url, pref) {

        var idStr = 'gadgetArea-preview';
        if ($('#' + idStr).length) {
            UESContainer.removeGadget(idStr);
        }
        parentEl.html('<div id="' + idStr + '">');
        UESContainer.renderGadget(idStr, url, pref || {}, function (gadgetInfo) {
            $('.gadget-preview-loader').fadeOut("fast");
        });
    }

    function refreshGadget(iframe) {
        if (isPortal) {
            var parentDiv = iframe.parents('div');

            iframe.ready(function () {
                iframe.height(parentDiv.parents('li').height() - 90);
            });

            iframe.get(0) && iframe.get(0).contentDocument.location.reload(true);
            return;
        }

        var parentLi = $(iframe).closest('li');

        $(iframe).ready(function () {

            $(iframe).height(parentLi.height() - 90);
        });

        if (typeof $(iframe).get(0) != 'undefined') {
            $(iframe).get(0).contentDocument.location.reload(true);
        }

    }

    function getLayoutFormat(layoutType) {
        var layoutFormat;
        switch (layoutType) {
            case 'rows':
                layoutFormat = [
                    {
                        "x": 1,
                        "y": 2,
                        "width": 6,
                        "height": 2
                    },
                    {
                        "x": 1,
                        "y": 4,
                        "width": 6,
                        "height": 2
                    },
                    {
                        "x": 1,
                        "y": 6,
                        "width": 6,
                        "height": 2
                    }
                ];
                break;
            case 'columns':
                layoutFormat = [
                    {
                        "x": 1,
                        "y": 2,
                        "width": 2,
                        "height": 6
                    },
                    {
                        "x": 3,
                        "y": 2,
                        "width": 2,
                        "height": 6
                    },
                    {
                        "x": 5,
                        "y": 2,
                        "width": 2,
                        "height": 6
                    }
                ];
                break;

            case 'composite':

                layoutFormat = [
                    {
                        "x": 1,
                        "y": 2,
                        "width": 2,
                        "height": 4
                    },
                    {
                        "x": 3,
                        "y": 2,
                        "width": 4,
                        "height": 1
                    },
                    {
                        "x": 3,
                        "y": 3,
                        "width": 4,
                        "height": 3
                    }
                ];
                break;
            default:
            case 'grid':

                layoutFormat = [
                    {
                        "x": 1,
                        "y": 2,
                        "width": 2,
                        "height": 2
                    },
                    {
                        "x": 3,
                        "y": 2,
                        "width": 2,
                        "height": 2
                    },
                    {
                        "x": 5,
                        "y": 2,
                        "width": 2,
                        "height": 2
                    },
                    {
                        "x": 1,
                        "y": 4,
                        "width": 2,
                        "height": 2
                    },
                    {
                        "x": 3,
                        "y": 4,
                        "width": 2,
                        "height": 2
                    },
                    {
                        "x": 5,
                        "y": 4,
                        "width": 2,
                        "height": 2
                    }
                ];
                break;

        }
        return layoutFormat;
    }

    $("#btn-exit-editor").click(function () {
        $('.sub-navbar-designer').slideUp("fast", function () {
            changeMode('view');

        });
    });

    $('#btn-add-dummy-gadget').click(function (e) {
        e.preventDefault();
        var $dummy = $('#dummy-size');
        var w = Number($dummy.attr('data-w'));
        var h = Number($dummy.attr('data-h'));

        if (isPortal) {
            var widget = layout.add_widget(itemTmp(), w, h, 1, 2);
        } else {
            var widget = layout.add_widget(widgetTemplate(), w, h, 1, 2);
        }

        registerEventsToWidget(widget);
        $('.dropdown.open .dropdown-toggle').dropdown('toggle');
    });

    $('#btn-preview-dash').click(function () {
        if ($(this).data('tooltip') == 'hide') {
            var dashboard = $('#inp-dashboard').val();
            var win = window.open('/' + dashboard, '_blank');
            win.focus();
        }
    });

    $("#btn-exit-view").click(function () {
        $('.sub-navbar-designer-view').slideUp("fast", function () {
            changeMode('design');
        });
    });

    $('.close-widget').live('click', function (e) {
        e.preventDefault();
        var widget = $(this).closest('.gs_w');
        layout.remove_widget($(widget));
        $(widget).remove();
        $('.gs_w').show();
    });

    function changeMode(mode) {
        flow_data.mode = mode;
        if (mode == 'view') {
            var title = $('#inp-designer-title').val();
            $('#dashboardName').find('span').text(title);
            $('#dashboardName').fadeIn();
            $('.sub-navbar-designer-view').fadeIn();
            layout.disable();
            $('#grid-guides').fadeOut("slow");
            $('.close-widget').hide();
            $('.show-widget-pref').hide();
            $('.layout_block .btn-add-gadget').hide();
            $('.layout_block').addClass('layout_block_view');
            $('.gadget-controls li:last-child').remove();

            $('.grid_header input').each(function () {
                var $this = $(this);
                $this.parent().parent().find('#header_label').show();
                //append('<h3>' + $this.val() + '</h3>');
                $this.parent().hide();
            });
            $('.btn-add-gadget-new').each(function(){
                var addBtn = $(this);
                addBtn.prop('disabled', true);

            });
            $('.btn-add-gadget-existing').each(function(){
                var addBtn = $(this);
                addBtn.prop('disabled', true);
            });
        } else if (mode == 'design') {
            var title = $('#dashboardName').find('span').text();
           // $('#inp-designer-title').val(title);
            $('#dashboardName').hide();
            $('.sub-navbar-designer').fadeIn();
            layout.enable();
            $('#grid-guides').fadeIn("slow");
            $('.close-widget').show();
            $('.show-widget-pref').each(function () {
                var $this = $(this);
                if ($this.parents('.grid_header').siblings('.designer-placeholder').length == 0) {
                    $this.show();
                }
            });
            $('.layout_block .grid_header h3').each(function () {
                var $this = $(this);
                if ($this.parent().parents('.grid_header').siblings('.designer-placeholder').length == 0) {
                    $this.parent().parent().find('#gadget_title_div').show();
                    //append('<input class="gadget-title-txt" value="' + $this.text() + '">');
                    $this.parent().hide();
                }
            });
            $('.layout_block .btn-add-gadget').show();
            $('.layout_block').removeClass('layout_block_view');
            $('.gadget-controls').append('<li><a href="#" class="close-widget"><i class="icon-remove"></i></a></li>');

            $('.btn-add-gadget-new').each(function(){
                var addBtn = $(this);
                addBtn.prop('disabled', false);
            });
            $('.btn-add-gadget-existing').each(function(){
                var addBtn = $(this);
                addBtn.prop('disabled', false);
            });
        }
    }

    var formArrayToPref = function (a) {
        var o = {};
        $.each(a, function () {
            if (o[this.name] !== undefined) {
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

    $('.show-widget-pref').live('click', function (e) {
        e.preventDefault();
        var $this = $(this);
        var widget = $this.closest('.gs_w');
        var id = widget.find(".add-gadget-item > div").attr('id');
        var info = UESContainer.getGadgetInfo(id);
        if (info) {
            var prefCont = widget.find('.gadget-pref-cont');

            var hidePref = function () {
                prefCont.empty();
                prefCont.hide();
                widget.find('.grid_header_controls').removeClass('grid_header_controls-show');
                $this.attr('data-collapse', true);
            };

            var savePref = function (e) {
                e.preventDefault();
                var newPref = formArrayToPref(prefCont.find('form').serializeArray());
                UESContainer.redrawGadget(id, {
                    prefs: newPref
                });
                hidePref();
            };

            if ($this.attr('data-collapse') == 'false') {
                hidePref();
                return;
            }

            var prefInfo = info.meta.userPrefs;
            var currentPref = info.opt.prefs || {};
            var html = '<form>';

            for (prefName in prefInfo) {
                var pref = prefInfo[prefName];
                var prefId = 'gadget-pref-' + id + '-' + prefName;
                html += '<label  for="' + prefId + '">' + pref.displayName + '</label>';
                html += '<input name="' + prefName + '" type="text" id="' + prefId + '" value="' + (currentPref[prefName] || pref.defaultValue ) + '">';
            }
            html += '<br><button class="btn btn-cancel-pref">Cancel</button>';
            html += '<button class="btn btn-primary btn-save-pref">Save</button>';
            html += '</form>';
            prefCont.html(html);
            prefCont.find('.btn-cancel-pref').on('click', function (e) {
                e.preventDefault();
                hidePref();
            });
            prefCont.find('.btn-save-pref').on('click', savePref);
            prefCont.show();
            widget.find('.grid_header_controls').addClass('grid_header_controls-show');
            $this.attr('data-collapse', false);
        }
    });

    $('.expand-widget').live('click', function (e) {
        e.preventDefault();
        var widget = $(this).closest('.gs_w');
        widget.addClass('maximized-view');
        var widgetEl = widget.get(0);
        $('.gs_w').each(function (i, el) {
            if (el != widgetEl) {
                $(el).hide();
            }
        });
        UESContainer.maximizeGadget(widget.find(".add-gadget-item > div").attr('id'));
    });

    // TODO: also close on ESC
    $('.shrink-widget').live('click', function (e) {
        e.preventDefault();
        var widget = $(this).closest('.gs_w');
        widget.removeClass('maximized-view');
        $('.gs_w').show();
        UESContainer.restoreGadget(widget.find(".add-gadget-item > div").attr('id'));
    });

    function checkMode() {
        if (isPortal) {
            var mode = $('#inp-view-mode').val();
            changeMode(mode);
        } else {
            changeMode('view');
        }
    }

    // Hides the 3 static gridster widgets placed at the top.
    // placing static widgets was a fix for gridster responsive bug
    // https://github.com/ducksboard/gridster.js/pull/77
    function setGridOffsetTop() {
        var sizey = parseInt($('.static').height());

        $('.layouts_grid').animate({
            'margin-top': "-" + (sizey - 80) + "px"
        });

    }

    $('#btn-save').click(function (e) {

        if (isPortal) {
            var dashboard = $('#inp-dashboard').val();
            var title = $('#inp-designer-title').val();
            var data = {
                title: title,
                widgets: layout.serialize()
            };

            var icon = $(this).find('i');
            icon.removeClass().addClass('icon-spinner icon-spin');

            $.post('apis/dashboard/' + dashboard, {
                layout: JSON.stringify(data)
            }).done(function (response) {
                    setTimeout(function () {
                        icon.removeClass().addClass('icon-save');
                        $("#btn-preview-dash").removeClass('disabled').tooltip('destroy').data('tooltip', 'hide');
                    }, 6000);
                }).fail(function (xhr, textStatus, errorThrown) {
                    icon.removeClass().addClass('icon-save');
                    // Session unavailable
                    if (xhr.status == 401) {
                        showAlert('Session timed out. Please login again.', 'alert-error', '.alert-bar');
                    } else {
                        showAlert('Error occured while saving dashboard. Please retry or re-login.', 'alert-error', '.alert-bar');
                    }
                });

        } else {
            e.preventDefault();

            var icon = $(this).find('i');
            icon.removeClass().addClass('icon-spinner icon-spin');

            var dashboard = $('#inp-dashboard').val();
            var title = $('#inp-designer-title').val();
            //var widgets = JSON.stringify(layout.serialize());
            var widgets = layout.serialize();
            widgets.splice(0, 4);
            $('.layout_block .grid_header h3').each(function () {
                var $this = $(this);
                if ($this.parent().parents('.grid_header').siblings('.designer-placeholder').length == 0) {
                    var editedTitle = $this.parent().parent().find('input').val();
                    $this.text(editedTitle);
                }
            });

            var _layout = {
                title: title,
                widgets: widgets
            };

            $.post('apis/ues/layout/' + dashboard, {
                layout: JSON.stringify(_layout)
            },function (result) {
                if (result) {
                    setTimeout(function () {
                        icon.removeClass().addClass('icon-save');
                    }, 1500);
                }
            }).error(function (error) {
                    console.log(error);
                });
        }

    });

    UESContainer.renderGadget('store-gadget-div', portalGadgets.gadgetTemplates);
    UESContainer.renderGadget('store-gadget-div2', portalGadgets.store);

    $('button[data-toggle=tooltip]').tooltip();

    $(window).bind('resize', resize);
    $(window).bind('load', checkMode);

    drawGadgets();

});
