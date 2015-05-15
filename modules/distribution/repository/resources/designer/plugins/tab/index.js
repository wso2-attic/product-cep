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

//csj
ues.plugin('tab', function (plugin, data) {
    plugin.html = function () {

    };
    plugin.js = function (el, data) {

    };
    plugin.css = function () {

    };
    el.html(ues.hbs('index.hbs', data));
    el.on('click', 'a', function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    $('a:first', el).tab('show');
});

//ssj or index.js
ues.plugin('tag', 'html', function (el, data, ues) {
    el.html(ues.render('index.hbs', data));
    el.html('<h1>' + data.name + '</h1>');
    el.on('click', 'a', function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    $('a:first', el).tab('show');
});

//ssj or index.js
ues.plugin('tag', 'js', function (el) {
    return ''
});

//ssj or index.css
ues.plugin('tag', 'css', function () {

});