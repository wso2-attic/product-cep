f/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

var debugObject; // assign object and debug from browser console, this is for debugging purpose , unless this var is unused
var showPathFlag = false; // Flag to hold the status of draw objects path
var currentSpatialObjects = {};
var selectedSpatialObject; // This is set when user search for an object from the search box
var websocket = new WebSocket('ws://localhost:9764/outputwebsocket/DefaultWebsocketOutputAdaptor/geoDataEndPoint');

websocket.onopen = function () {
    $.UIkit.notify({
        message: 'You Are Connectedto Map Server!!',
        status: 'warning',
        timeout: 1000,
        pos: 'bottom-left'
    });
};

websocket.onmessage = function processMessage(message) {
    var geoJsonFeature = $.parseJSON(message.data);
    if (geoJsonFeature.id in currentSpatialObjects) {
        var excitingObject = currentSpatialObjects[geoJsonFeature.id];
        excitingObject.update(geoJsonFeature);
    }
    else {
        var receivedObject = new SpatialObject(geoJsonFeature);
        currentSpatialObjects[receivedObject.id] = receivedObject;
        currentSpatialObjects[receivedObject.id].addTo(map);
    }
};

var normalIcon = L.icon({
    iconUrl: "assets/img/markers/arrow_normal.png",
    shadowUrl: false,
    iconSize: [24, 24],
    iconAnchor: [+12, +12],
    popupAnchor: [-2, -5] //[-3,-76]
});

var stopIcon = L.icon({
    iconUrl: "assets/img/markers/stopIcon.png",
    shadowUrl: false,
    iconSize: [24, 24],
    iconAnchor: [+12, +12],
    popupAnchor: [-2, -5] //[-3,-76]
});
var alertedIcon = L.icon({
    iconUrl: "assets/img/markers/arrow_alerted.png",
    shadowUrl: false,
    iconSize: [24, 24],
    iconAnchor: [+12, +12],
    popupAnchor: [-2, -5] //[-3,-76]
});
var offlineIcon = L.icon({
    iconUrl: "assets/img/markers/arrow_offline.png",
    iconSize: [24, 24],
    iconAnchor: [+12, +12],
    popupAnchor: [-2, -5] //[-3,-76]
});
var defaultIcon = L.icon({
    iconUrl: "assets/img/markers/default_icons/marker-icon.png",
    iconSize: [24, 24],
    iconAnchor: [+12, +12],
    popupAnchor: [-2, -5] //[-3,-76]
});

function SpatialObject(geoJSON) {
    this.id = geoJSON.id;

    // Have to store the coordinates , to use when user wants to draw path
    this.pathGeoJsons = []; // GeoJson standard MultiLineString(http://geojson.org/geojson-spec.html#id6) can't use here because this is a collection of paths(including property attributes)
    this.path = []; // Path is an array of sections, where each section is a notified state of the path
//    {
//        "type": "LineString",
//        "coordinates": []
//    };


    // Private variable as a LineStringFeature template
    var createLineStringFeature = function (state, information, coordinates) {
        return {"type": "Feature",
            "properties": {
                "state": state,
                "information": information
            },
            "geometry": {
                "type": "LineString",
                "coordinates": [coordinates]
            }
        };
    };

    this.speedHistory = ['speed']; // TODO: fetch this array from backend DB rather than keeping as in-memory array
    this.geoJson = L.geoJson(geoJSON, {
        pointToLayer: function (feature, latlng) {
            return L.marker(latlng, {icon: normalIcon, iconAngle: this.heading});
        }
    }); // Create Leaflet GeoJson object

    this.marker = this.geoJson.getLayers()[0];
    this.marker.options.title = this.id;

    this.popupTemplate = $('#markerPopup');
    this.marker.bindPopup(this.popupTemplate.html());

    /* Method definitions */
    this.addTo = function (map) {
        this.geoJson.addTo(map);
    };
    this.setSpeed = function (speed) {
        this.speed = speed;
        this.speedHistory.push(speed);
        if (this.speedHistory.length > 20) {
            this.speedHistory.splice(1, 1);
        }
    };

    this.stateIcon = function () {
        // Performance of if-else, switch or map based conditioning http://stackoverflow.com/questions/8624939/performance-of-if-else-switch-or-map-based-conditioning
        switch (this.state) {
            case "NORMAL":
                return normalIcon;
                break;
            case "ALERTED":
                return alertedIcon;
                break;
            case "OFFLINE":
                return offlineIcon;
                break;
            default:
                return defaultIcon;
        }
    };
    this.updatePath = function (LatLng) {
        this.path[this.path.length - 1].addLatLng(LatLng); // add LatLng to last section
//        try{
//            this.path[this.path.length - 1].addLatLng(LatLng); // add to last section
//        }catch (error){
//            this.path[this.path.length - 1].addTo(map); // TODO: this will only add the last path section which is not added to map , middle sections ???
//        }
    };
    this.drawPath = function () {
        var previousSectionLastPoint = []; // re init all the time when calls the function
        if (this.path.length > 0) {
            this.removePath();
//            throw "geoDashboard error: path already exist,remove current path before drawing a new path, if need to update LatLngs use setLatLngs method instead"; // Path already exist
        }
        for (var lineString in this.pathGeoJsons) {
            var currentSectionState = this.pathGeoJsons[lineString].properties.state;
            var currentSection = new L.polyline(this.pathGeoJsons[lineString].geometry.coordinates, getSectionStyles(currentSectionState)); // Create path object when and only drawing the path (save memory) TODO: if need directly draw line from geojson

            var currentSectionFirstPoint = this.pathGeoJsons[lineString].geometry.coordinates[0];
            console.log("DEBUG: previousSectionLastPoint = " + previousSectionLastPoint + " currentSectionFirstPoint = " + currentSectionFirstPoint);
            previousSectionLastPoint.push(currentSectionFirstPoint);
            var sectionJoin = new L.polyline(previousSectionLastPoint, getSectionStyles());
            previousSectionLastPoint = [this.pathGeoJsons[lineString].geometry.coordinates[this.pathGeoJsons[lineString].geometry.coordinates.length - 1]];
            sectionJoin.addTo(map);
            this.path.push(sectionJoin);
            console.log("DEBUG: Alert Information: " + this.pathGeoJsons[lineString].properties.information);
            currentSection.bindPopup("Alert Information: " + this.pathGeoJsons[lineString].properties.information);
            currentSection.addTo(map);
            this.path.push(currentSection);
        }
    };

    this.removePath = function () {
        for (var section in this.path) {
            map.removeLayer(this.path[section]);
        }
        this.path = []; // Clear the path layer (save memory)
    };

    var pathColor;
    var getSectionStyles = function (state) {
        switch (state) {
            case "NORMAL":
                pathColor = 'blue'; // Scope of function
                break;
            case "ALERTED":
                pathColor = 'red';
                break;
            case "WARNING":
                pathColor = 'orange';
                break;
            case "OFFLINE":
                pathColor = 'green';
                break;
            default:
                return {color: "#19FFFF", weight: 8};
        }
        return {color: pathColor, weight: 8};
    };

    this.update = function (geoJSON) {
        this.latitude = geoJSON.geometry.coordinates[1];
        this.longitude = geoJSON.geometry.coordinates[0];
        this.setSpeed(geoJSON.properties.speed);
        this.state = geoJSON.properties.state;
        this.heading = geoJSON.properties.heading;

        this.information = geoJSON.properties.information;

        if (geoJSON.properties.notify) {
            notifyAlert("Object ID: <span style='color: blue;cursor: pointer' onclick='focusOnSpatialObject(" + this.id + ")'>" + this.id + "</span> change state to: <span style='color: red'>" + geoJSON.properties.state + "</span> Info : " + geoJSON.properties.information);
            var newLineStringGeoJson = createLineStringFeature(this.state, this.information, [this.latitude, this.longitude]);
            this.pathGeoJsons.push(newLineStringGeoJson);

            // only add the new path section to map if the spatial object is selected
            if (selectedSpatialObject == this.id) {
                var newPathSection = new L.polyline(newLineStringGeoJson.geometry.coordinates, getSectionStyles(geoJSON.properties.state));
                newPathSection.bindPopup("Alert Information: " + newLineStringGeoJson.properties.information);

                // Creating two sections joint
                var lastSection = this.path[this.path.length - 1].getLatLngs();
                var joinLine = [lastSection[lastSection.length - 1], [this.latitude, this.longitude]];
                var sectionJoin = new L.polyline(joinLine, getSectionStyles());

                this.path.push(sectionJoin);
                this.path.push(newPathSection); // Order of the push matters , last polyLine object should be the `newPathSection` not the `sectionJoin`

                sectionJoin.addTo(map);
                newPathSection.addTo(map);
            }
        }

        // Update the spatial object leaflet marker
        this.marker.setLatLng([this.latitude, this.longitude]);
        this.marker.setIconAngle(this.heading);
        this.marker.setIcon(this.stateIcon());

        try {
            // To prevent conflicts in
            // Leaflet(http://leafletjs.com/reference.html#latlng) and geoJson standards(http://geojson.org/geojson-spec.html#id2),
            // have to do this swapping, but the resulting geoJson in not upto geoJson standards
            this.pathGeoJsons[this.pathGeoJsons.length - 1].geometry.coordinates.push([geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
        }
        catch (error) {
            console.log("DEBUG: Dam error = " + error);
            // TODO: optimize if can , catch block execute only when initializing the object (suggestion do this in object initialization stage but then redundant LatLng)
            newLineStringGeoJson = createLineStringFeature(this.state, this.information, [geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
            this.pathGeoJsons.push(newLineStringGeoJson);
        }

        if (selectedSpatialObject == this.id) {
            this.updatePath([geoJSON.geometry.coordinates[1], geoJSON.geometry.coordinates[0]]);
            chart.load({columns: [this.speedHistory]});
            map.setView([this.latitude, this.longitude]);
        }

        // TODO: remove consecutive two lines object ID never change with time + information toggled only when `geoJSON.properties.notify` true (done in CEP side)
        this.popupTemplate.find('#objectId').html(this.id);
        this.popupTemplate.find('#information').html(this.information);

        this.popupTemplate.find('#speed').html(this.speed);
        this.popupTemplate.find('#heading').html(this.heading);
        this.marker.setPopupContent(this.popupTemplate.html())


    };
    this.update(geoJSON);
    return this;
}

function notifyAlert(message) {
    $.UIkit.notify({
        message: "Alert: " + message,
        status: 'warning',
        timeout: 3000,
        pos: 'bottom-left'
    });
}

function Alert(type, message, level) {
    this.type = type;
    this.message = message;
    if (level)
        this.level = level;
    else
        this.level = 'info';

    this.notify = function () {
        $.UIkit.notify({
            message: this.level + ': ' + this.type + ' ' + this.message,
            status: 'info',
            timeout: 1000,
            pos: 'bottom-left'
        });
    }
}


/* All the remote calls to backend server*/

/* close all opened modals and side pane */
function addTileUrl() {
    /* TODO: add validation, check for empty url and names*/
    tileUrl = $('#tileUrl').val();
    urlName = $('#tileName').val();
    maxzoom = $('#maxzoom').val();
    subdomains = $('#sub_domains').val();
    attribution = $('#data_attribution').val();
    /* Add to base layers*/
    var newTileLayer = L.tileLayer(tileUrl, {
        maxZoom: parseInt(maxzoom),
        subdomains: subdomains.split(','),
        attribution: attribution
    });
    layerControl.addBaseLayer(newTileLayer, urlName);

    inputs = layerControl._form.getElementsByTagName('input');
    inputsLen = inputs.length;
    for (i = 0; i < inputsLen; i++) {
        input = inputs[i];
        obj = layerControl._layers[input.layerId];
        if (layerControl._map.hasLayer(obj.layer)) {
            map.removeLayer(obj.layer);
        }
    }
    map.addLayer(newTileLayer);

    /* Do ajax save */
    var data = {
        'url': tileUrl,
        'name': urlName,
        'attribution': attribution,
        'maxzoom': maxzoom,
        'subdomains': subdomains
    };
    var serverUrl = "controllers/tile_servers.jag";
    // TODO: If failure happens notify user about the error message
    $.post(serverUrl, data, function (response) {
        $.UIkit.notify({
            message: '<span style="color: dodgerblue">' + response + '</span>',
            status: 'success',
            timeout: 3000,
            pos: 'top-center'
        });
        closeAll();
    });

// TODO: Show a preview of newly added tileserver map
//    var mapPreview = L.map('mapPreview', {
//        zoom: 10,
//        center: [6.934846, 79.851980],
//    });
//    L.tileLayer(tileUrl).addTo(mapPreview);

}

var defaultOSM = L.tileLayer("http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: 'Map data © OpenStreetMap contributors <a href="http://openstreetmap.org/" target="_blank">Openstreetmap</a> <img src="http://developer.mapquest.com/content/osm/mq_logo.png">. Map data (c) <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a> contributors, CC-BY-SA.'
});

var baseLayers = {
    "Open Street Maps": defaultOSM
};
function getTileServers() {
    //For reference: returning JSON from server
    // {"serverId" : 44, "url" : "ffsafsa", "name" : "sadsa", "subdomains" : "asfasfsa", "attribution" : "dsfdsfdsf", "maxzoom" : 12}
    $.getJSON("controllers/tile_servers.jag?serverId=all", function (data) {
        $.each(data, function (key, val) {
            $.UIkit.notify({
                message: 'Loading... <span style="color: #ccfcff">' + val.name + '</span>' +
                    ' URL: <span style="color: #00ff00">' + val.url + '</span>',
                status: 'info',
                timeout: 2000,
                pos: 'bottom-left'
            });
            //baseLayers[val.name]
            var newTileLayer = L.tileLayer(
                val.url, {
                    maxZoom: val.maxzoom, // TODO: if no maxzoom level do not set this attribute
                    subdomains: val.subdomains.split(','), // TODO: if no subdomains do not set this attribute
                    attribution: val.attribution
                }
            );
            layerControl.addBaseLayer(newTileLayer, val.name); // TODO: implement single method for #20  and this and do validation
        });
    });

}

function addWmsEndPoint() {
    serviceName = $('#serviceName').val();
    layers = $('#layers').val();
    wmsVersion = $('#wmsVersion').val();
    serviceEndPoint = $('#serviceEndPoint').val();
    outputFormat = $('#outputFormat').val();

    wmsLayer = L.tileLayer.wms(serviceEndPoint, {
        layers: layers.split(','),
        format: outputFormat ? outputFormat : 'image/png',
//        version: wmsVersion,
        transparent: true,
        opacity: 0.4});

    layerControl.addOverlay(wmsLayer, serviceName, "Web Map Service layers");
    map.addLayer(wmsLayer);
    var data = {
        'serviceName': serviceName,
        'layers': layers,
        'wmsVersion': wmsVersion,
        'serviceEndPoint': serviceEndPoint,
        'outputFormat': outputFormat
    };
    var serverUrl = "controllers/wms_endpoints.jag";
    // TODO: If failure happens notify user about the error message
    $.post(serverUrl, data, function (response) {
        $.UIkit.notify({
            message: '<span style="color: dodgerblue">' + response + '</span>',
            status: 'success',
            timeout: 3000,
            pos: 'top-center'
        });
        closeAll();
    });
}

function getWms() {
    // For refference {"wmsServerId" : 1, "serviceUrl" : "http://{s}.somedomain.com/blabla/{z}/{x}/{y}.png", "name" : "Sample server URL", "layers" : "asdsad,sd,adasd,asd", "version" : "1.0.2", "format" : "sadasda/asdas"}
    $.getJSON("controllers/wms_endpoints.jag?serverId=all", function (data) {
        $.each(data, function (key, val) {

            wmsLayer = L.tileLayer.wms(val.serviceUrl, {
                layers: val.layers.split(','),
                format: val.format ? val.format : 'image/png',
                version: val.version,
                transparent: true,
                opacity: 0.4});
            layerControl.addOverlay(wmsLayer, val.name, "Web Map Service layers");
        });
    });
}

function setSpeedAlert() {
    var speedAlertValue = $("#speedAlertValue").val();
    data = {
        'parseData': JSON.stringify({'speedAlertValue': speedAlertValue}), // parseKey : parseValue pair , this key pair is replace with the key in the template file
        'executionPlan': 'speed',
        'customName': null,
        'cepAction': 'edit' // TODO: what if setting speed alert for the first time ?? that should be a deployment ? try 'edit' if fails 'deploy' , need to handle at the jaggery back end
    };
    $.post('controllers/set_alerts.jag', data, function (response) {
        $.UIkit.notify({
            message: '<span style="color: dodgerblue">' + response.status + '</span><br>' + response.message,
            status: (response.status == 'success' ? 'success' : 'danger'),
            timeout: 3000,
            pos: 'top-center'
        });
        closeAll();
    }, 'json');
}


function setWithinAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
     
    var selectedAreaGeoJson = JSON.stringify(map._layers[leafletId].toGeoJSON().geometry).replace(/"/g, "'");
    var queryName = $("#queryName").val();
    var areaName = $("#areaName").val();
    data = {
        'parseData': JSON.stringify({'geoFenceGeoJSON': selectedAreaGeoJson, 'executionPlanName': createExecutionPlanName(queryName), 'areaName': areaName}),
        'executionPlan': 'within',
        'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
        'queryName': queryName,
        'cepAction': 'deploy'
    };
    $.post('controllers/set_alerts.jag', data, function (response) {
        $.UIkit.notify({
            message: '<span style="color: dodgerblue">' + response.status + '</span><br>' + response.message,
            status: (response.status == 'success' ? 'success' : 'danger'),
            timeout: 3000,
            pos: 'top-center'
        });
        closeAll();
        closeWithinTools(leafletId);
    }, 'json');
}

function removeGeoFence(geoFenceElement) {
    var queryName = $(geoFenceElement).attr('data-queryName');
    var areaName = $(geoFenceElement).attr('data-areaName');

    data = {
        'executionPlanName': createExecutionPlanName(queryName),
        'queryName': queryName,
        'cepAction': 'undeploy'

    };
    $.post('controllers/remove_alerts.jag', data, function (response) {
        $.UIkit.notify({
            message: '<span style="color: dodgerblue">' + response.status + '</span><br>' + response.message,
            status: (response.status == 'success' ? 'success' : 'danger'),
            timeout: 3000,
            pos: 'top-center'
        });
        closeAll();
    }, 'json');
}


function getAlertsHistory(objectId) {
    $.getJSON("controllers/get_alerts_history.jag?objectId=" + objectId, function (data) {
        var alertsContainer = $('#showAlertsArea').empty();
        $.each(data, function (key, val) {
            var alertDOMElement = document.createElement('a'); // Reason for using document.createElement (performance issue) http://stackoverflow.com/questions/268490/jquery-document-createelement-equivalent
            // TODO: define central state definition if needed to cahnge then it would be only one place change, same state switch has been used in websocket , spatialObject prototype
            switch (val.state) {
                case "NORMAL":
//                    $(alertDOMElement).addClass("list-group-item list-group-item-info");
                    return;
                case "WARNING":
                    $(alertDOMElement).addClass("list-group-item list-group-item-warning");
                    break;
                case "ALERTED":
                    $(alertDOMElement).addClass("list-group-item list-group-item-danger");
                    break;
                case "OFFLINE":
                    $(alertDOMElement).addClass("list-group-item list-group-item-success");
                    break;
            }
            $(alertDOMElement).html(val.information);
            $(alertDOMElement).css({marginTop : "5px"});
            $(alertDOMElement).attr('onClick', 'showAlertInMap(this)');

            // Set HTML5 data attributes for later use
            $(alertDOMElement).attr('data-id', val.id);
            $(alertDOMElement).attr('data-latitude', val.latitude);
            $(alertDOMElement).attr('data-longitude', val.longitude);
            $(alertDOMElement).attr('data-state', val.state);
            $(alertDOMElement).attr('data-information', val.information);

            alertsContainer.append(alertDOMElement);
        });
    });
}

// TODO:this is not a remote call , move this to application.js
function createExecutionPlanName(queryName) {
    return 'geo_within' + (queryName ? '_' + queryName : '') + '_alert'; // TODO: value of the `queryName` can't be empty, because it will cause name conflicts in CEP, have to do validation(check not empty String)
}

// TODO:this is not a remote call , move this to application.js
function closeAll() {
    $('.modal').modal('hide');
    setTimeout(function () {
        $.UIkit.offcanvas.hide()
    }, 100);
}

var drawControl;
function openWithinTools() {
    closeAll();
    $.UIkit.notify({
        message: "Please draw the required area on the map",
        status: 'success',
        timeout: 3000,
        pos: 'top-center'
    });

    L.Control.RemoveAll = L.Control.extend(
        {
            options: {
                position: 'topleft'
            },
            onAdd: function (map) {
                var controlDiv = L.DomUtil.create('div', 'leaflet-draw-toolbar leaflet-bar');
                L.DomEvent
                    .addListener(controlDiv, 'click', L.DomEvent.stopPropagation)
                    .addListener(controlDiv, 'click', L.DomEvent.preventDefault)
                    .addListener(controlDiv, 'click', function () {
                        controlDiv.remove();
                        drawControl.removeFrom(map);
                        drawnItems.clearLayers();
                    });

                var controlUI = L.DomUtil.create('a', 'fa fa-times fa-lg drawControlCloseButton', controlDiv);
                $(controlUI).css("background-image", "none"); // Remove default control icon
                // TODO: bad usage of .hover() use CSS instead
                $(controlUI).mouseenter(function () {
                    $(this).css("color", "red");
                }).mouseleave(function () {
                    $(this).css("color", "black")
                });

                controlUI.title = 'Close drawer tools';
                controlUI.href = '#';
                return controlDiv;
            }
        });
    ;
    var removeAllControl = new L.Control.RemoveAll();
    map.addControl(removeAllControl);


    // Initialise the FeatureGroup to store editable layers
    var drawnItems = new L.FeatureGroup();
    map.addLayer(drawnItems);
    var defaultMarker = L.Icon.extend({
        options: {
            shadowUrl: 'assets/img/markers/default_icons/marker-shadow.png',
            iconUrl: 'assets/img/markers/default_icons/marker-icon.png'
        }
    });
    // Initialise the draw control and pass it the FeatureGroup of editable layers
    drawControl = new L.Control.Draw({
        draw: {
//            polyline: {
//                shapeOptions: {
//                    color: '#f357a1',
//                    weight: 10
//                }
//            }
            polygon: {
                allowIntersection: false, // Restricts shapes to simple polygons
                drawError: {
                    color: '#e1e100', // Color the shape will turn when intersects
                    message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
                },
                shapeOptions: {
                    color: '#ff0043'
                }
            },
            rectangle: {
                shapeOptions: {
                    color: '#002bff'
                }
            },
            polyline: false,
            circle: false, // Turns off this drawing tool
            marker: false // Markers are not applicable for within geo fencing
//            marker: {
//                icon: new defaultMarker()
//            }
        },
        edit: {
            featureGroup: drawnItems
        }
    });
    map.addControl(drawControl);

    map.on('draw:created', function (e) {
        var type = e.layerType,
            layer = e.layer;

        if (type === 'marker') {
            // Do marker specific actions
        }
        drawnItems.addLayer(layer);
        createPopup(layer);

    });

}

function createPopup(layer) {
    var popupTemplate = $('#setWithinAlert');
    popupTemplate.find('#exportGeoJson').attr('leaflet_id', layer._leaflet_id);
    popupTemplate.find('#editGeoJson').attr('leaflet_id', layer._leaflet_id);
    popupTemplate.find('#addWithinAlert').attr('leaflet_id', layer._leaflet_id);
    layer.bindPopup(popupTemplate.html(), {closeOnClick: false, closeButton: false}).openPopup();
    // transparent the layer .leaflet-popup-content-wrapper
    $(layer._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");
}

function closeWithinTools(leafletId) {
    map.removeLayer(map._layers[leafletId]);
    map.removeControl(drawControl);
    console.log("DEBUG: closeWithinTools(leafletId) = "+leafletId);
}

/* Export selected area on the map as a json encoded geoJson standard file, no back-end calls simple HTML5 trick ;) */
function exportToGeoJSON(link, content) {
    // HTML5 features has been used here
    geoJsonData = 'data:application/json;charset=utf-8,' + encodeURIComponent(content);
    // TODO: replace closest()  by using persistence id for templates, template id prefixed by unique id(i.e leaflet_id)
    fileName = $(link).closest('form').find('#areaName').val() || 'geoJson';
    $(link).attr({
        'href': geoJsonData,
        'target': '_blank',
        'download': fileName + '.json' // Use the fence name given by the user as the file name of the JSON file
    });
}

$(function () {
    $("#importGeoJsonFile").change(function () {
        var importedFile = this.files[0];
        var reader = new FileReader();
        reader.readAsText(importedFile);
        reader.onload = function (e) {
            $("#enterGeoJson").text(e.target.result.toString());
        };
    });
});
function importGeoJson() {
//    inputFile = $('#importGeoJsonFile')[0].files[0];
    var updatedGeoJson;
//    // If the user has upload a file using the file browser this condition become true
//    if(inputFile){
//        // create HTML5 reader
//        fileName = inputFile.name.split('.json')[0];// TODO: put this file name (after removing the extension .json) in to the fence name #areaName input
//        var reader = new FileReader();
//        reader.readAsText(inputFile);
//        reader.onload = function(e) {
//            // browser completed reading file - display it
//            // Wait until the state become ready(complete the file read)
//            while(e.target.readyState != FileReader.DONE);
//            // Take the content of the file
//            // TODO: do validation, check wheather a valid JSON || GeoJSON file if not $.notify the user
//            updatedGeoJson = e.target.result.toString();
//            // TODO: check the uploded GeoJSON file for the type (circle, polygon , line, etc ) and update only if the drawn element is match with the uploaded geoJSON else $.notify the user
//            updateDrawing(updatedGeoJson);
//        };
//    }
//    // else use the edited text on the textarea
//    else{
    updatedGeoJson = $('#enterGeoJson').val();
    updateDrawing(updatedGeoJson);
//    }
}

function updateDrawing(updatedGeoJson) {
    updatedGeoJson = JSON.parse(updatedGeoJson);
    // Pop the last LatLng pair because according to the geoJSON standard it use complete round LatLng set to store polygon coordinates
    updatedGeoJson.geometry.coordinates[0].pop();
    var leafletLatLngs = [];
    $.each(updatedGeoJson.geometry.coordinates[0], function (idx, pItem) {
        leafletLatLngs.push({lat: pItem[1], lng: pItem[0]});
    });

    var polygon = new L.Polygon(leafletLatLngs);
    polygon.editing.enable();
    map.addLayer(polygon);
    createPopup(polygon);

    /*
     // For reffrence TODO: remove if not use
     currentDrawingLayer.setLatLngs(leafletLatLngs);
     layerId = $(button).attr('leaflet_id');
     console.log(layerId);
     currentDrawingLayer = map._layers[layerId];

     // At least a line or polygon must have 2 points so try the following with '0', '1',not more that that could give unexpected errors
     currentDrawingLayer._popup.setLatLng(leafletLatLngs[1]);
     // TODO: Use rails a-like id generating method to identify each copy of the the templates uniquely i.e marker_popup_{leaflet_layer_id}
     //$(button).closest('form').find('#areaName').val(fileName);
     */
    closeAll();

}


function viewFence(geoFenceElement) {
    geoJson = JSON.parse($(geoFenceElement).attr('data-geoJson'));
    var queryName = $(geoFenceElement).attr('data-queryName');
    var areaName = $(geoFenceElement).attr('data-areaName');

    geoJson.coordinates[0].pop(); // popout the last coordinate set(lat,lng pair) due to circular chain
    var leafletLatLngs = [];
    $.each(geoJson.coordinates[0], function (idx, pItem) {
        leafletLatLngs.push({lat: pItem[1], lng: pItem[0]});
    });

    var polygon = new L.Polygon(leafletLatLngs);
    map.addLayer(polygon);

    $('#templateLoader').load("assets/html_templates/view_fence_popup.html #viewWithinAlert", function () {
        popupTemplate = $('#templateLoader').find('#viewWithinAlert');
        popupTemplate.find('#exportGeoJson').attr('leaflet_id', polygon._leaflet_id);
        popupTemplate.find('#hideViewFence').attr('leaflet_id', polygon._leaflet_id);
        popupTemplate.find('#viewAreaName').html(areaName);
        popupTemplate.find('#viewQueryName').html(queryName);

        polygon.bindPopup(popupTemplate.html(), {closeButton: false}).openPopup();
        // transparent the layer .leaflet-popup-content-wrapper
        $(polygon._popup._container.childNodes[0]).css("background", "rgba(255,255,255,0.8)");
        closeAll();
    });
}

function showAlertInMap(alertData) {

    var id = $(alertData).attr("data-id");
    var latitude = $(alertData).attr("data-latitude");
    var longitude = $(alertData).attr("data-longitude");
    var state = $(alertData).attr("data-state");
    var information = $(alertData).attr("data-information");

    var alertLatLngPoint = L.latLng(latitude,longitude);

    var alertOccouredArea = L.circle(alertLatLngPoint, 10, {
        color: '#FF9900',
        fillColor: '#FF00FF',
        fillOpacity: 0.5
    }).addTo(map);

    alertOccouredArea.bindPopup("Id: <b>"+id+"</b><br>"+
            "State: <b>"+state+"</b><br>"+
            "Information: <b>"+information+"</b><br>"
    ).openPopup();
    $(alertOccouredArea._popup._closeButton).on("click",function(){map.removeLayer(alertOccouredArea)});
    map.setView(alertLatLngPoint,18);

    clearFocus();

    /* TODO: for reference <Update lib or remove if not in use>: This `R`(RaphaelLayer: https://github.com/dynmeth/RaphaelLayer) library is dam buggy can't use it reliably */
    /*
     var alertPulse = new R.Pulse(
     alertLatLngPoint,
     8,
     {'stroke': '#FF9E0E', 'fill': '#FF0000'},
     {'stroke': '#FF3E2F', 'stroke-width': 3});
     map.addLayer(alertPulse);
     */


}
