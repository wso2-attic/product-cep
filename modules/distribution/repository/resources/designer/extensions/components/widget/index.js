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

(function () {

    var containerPrefix = 'ues-widget-';

    var containerId = function (id) {
        return containerPrefix + id;
    };

    var component = (ues.plugins.components['widget'] = {});

    component.create = function (sandbox, component, hub, done) {
        var html = '<h2>' + component.content.title + '</h2>';
        html += '<button class="ues-send btn btn-primary" type="button">Send</button>';
        sandbox.html(html);
        var id = containerId(component.id);
        var container = new OpenAjax.hub.InlineContainer(ues.hub, id, {
                Container: {
                    onSecurityAlert: function (source, alertType) {
                        //Handle client-side security alerts
                    },
                    onConnect: function (container) {
                        //Called when client connects
                    },
                    onDisconnect: function (container) {
                        //Called when client disconnects
                    }
                }
            }
        );

        var client = new OpenAjax.hub.InlineHubClient({
            HubClient: {
                onSecurityAlert: function (source, alertType) {
                }
            },
            InlineHubClient: {
                container: container
            }
        });

        var Hub = function (client, options) {
            this.client = client;
            this.options = options;
        };

        Hub.prototype.on = function (event, done) {
            var hub = this;
            var channel = hub.options.id + '.' + event;
            hub.client.subscribe(channel, function (topic, data, subscription) {
                done(data);
            });
            console.log('subscribed for channel:%s by %s', channel, hub.options.id);
        };

        Hub.prototype.emit = function (event, data) {
            console.log('publishing event:%s, data:%s by notifier:%s', event, data, this.options.id);
            this.client.publish(event, data);
        };

        client.connect(function (client, success, error) {
            var hub = new Hub(client, component);
            sandbox.on('click', '.ues-send', function () {
                hub.emit('user-country', 'LK');
                hub.emit('client-country', 'US');
                //hub.emit('country-code', {});
            });
            hub.on('state', function (state) {
                console.log(state);
            });
        });
        done(false, component);
    };

    component.update = function (sandbox, component, hub, done) {

    };

    component.destroy = function (sandbox, component, hub, done) {
        $(sandbox).remove('iframe');
    };

}());