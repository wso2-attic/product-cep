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

var ues = ues || {};
var store = {};

(function () {

    var assetsUrl = ues.utils.relativePrefix() + 'assets';

    var store = (ues.store = {});

    store.gadget = function (id, cb) {
        $.get(assetsUrl + '/' + id + '?type=gadget', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.gadgets = function (paging, cb) {
        $.get(assetsUrl + '?start=' + paging.start + '&count=' + paging.count + '&type=gadget', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.layout = function (id, cb) {
        $.get(assetsUrl + '/' + id + '?type=layout', function (data) {
            cb(false, data);
        }, 'json');
    };

    store.layouts = function (paging, cb) {
        $.get(assetsUrl + '?start=' + paging.start + '&count=' + paging.count + '&type=layout', function (data) {
            cb(false, data);
        }, 'json');
    };


}());