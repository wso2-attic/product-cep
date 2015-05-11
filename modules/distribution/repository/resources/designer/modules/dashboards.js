/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var log = new Log();

var carbon = require('carbon');

//TODO: what happen when the context is changed or mapped via reverse proxy
var registryPath = function (id) {
    var path = '/_system/config/ues/dashboards';
    return id ? path + '/' + id : path;
};

var findOne = function (id) {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    var content = registry.content(registryPath(id));
    return JSON.parse(content);
};

var find = function () {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    var dashboards = registry.content(registryPath());
    var dashboardz = [];
    if(dashboards != null) {
        dashboards.forEach(function (dashboard) {
            dashboardz.push(JSON.parse(registry.content(dashboard)));
        });
    }
    return dashboardz;
};

var create = function (dashboard) {
    var usr = require('/modules/user.js');
    var user = usr.current();
    var server = new carbon.server.Server();
    var registry = new carbon.registry.Registry(server, {
        username: user.username,
        tenantId: user.tenantId
    });
    registry.put(registryPath(dashboard.id), {
        content: JSON.stringify(dashboard),
        mediaType: 'application/json'
    });
};