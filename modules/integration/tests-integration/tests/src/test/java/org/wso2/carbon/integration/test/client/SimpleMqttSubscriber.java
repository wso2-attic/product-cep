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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleMqttSubscriber implements MqttCallback {

    private MqttClient mqttClient;
    private MqttConnectOptions connOpt;
    private String url;
    private String topic;
    private int count = 0;

    public SimpleMqttSubscriber(String topic, String url) {
        this.url = url;
        this.topic = topic;
    }


    public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
    }


    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");

        count++;
    }


    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void runClient() {

        connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);


        try {
            mqttClient = new MqttClient(url, "SIMPLE-MQTT-SUB");
            mqttClient.setCallback(this);
            mqttClient.connect(connOpt);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Connected to " + url);
        try {
            int subQoS = 0;
            mqttClient.subscribe(topic, subQoS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            Thread.sleep(60000);
            mqttClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return count;
    }
}