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

package org.wso2.carbon.integration.test.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleMqttPublisher {

    private MqttClient myClient;
    private MqttConnectOptions connOpt;
    private String url;
    private String topic;

    public SimpleMqttPublisher(String topic, String url) {
        this.topic = topic;
        this.url = url;
    }

    public void runClient(String[] messages) {

        connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);

        try {
            myClient = new MqttClient(url, "SIMPLE-MQTT-PUB");
            myClient.connect(connOpt);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        System.out.println("Connected to " + url);

        for (int i = 0; i <= messages.length; i++) {
            String pubMsg = messages[i];
            int pubQoS = 1;
            MqttMessage message = new MqttMessage(pubMsg.getBytes());
            message.setQos(pubQoS);

            try {
                myClient.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        try {
            myClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}