/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.mqtt.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.*;

public class MqttConsumer implements MqttCallback {

    private static Log log = LogFactory.getLog(MqttConsumer.class);
    private MqttClient myClient;
    private static String url;
    private static String topic;

    public static void main(String[] args) {
        if (args.length == 2) {
            url = args[0];
            topic = args[1];
        } else {
            log.error("Please provide necessary parameters");
            return;
        }

        MqttConsumer smc = new MqttConsumer();
        smc.runClient();
    }


    @Override
    public void connectionLost(Throwable t) {
        log.warn("Connection lost!");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("Received Message -------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("------------------------------------------------------------------");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void runClient() {

        MqttConnectOptions connOpt = new MqttConnectOptions();

        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);

        try {
            myClient = new MqttClient(url, "SIMPLE-MQTT-SUB1");
            myClient.setCallback(this);
            myClient.connect(connOpt);
            log.info("Connected to " + url);
        } catch (MqttException e) {
            log.error("Exception when connecting to MQTT Server", e);
        }

        try {
            int subQoS = 0;
            myClient.subscribe(topic, subQoS);
        } catch (Exception e) {
            log.error("Exception when initiating the subscription", e);
        }

        try {
            // wait to ensure subscribed messages are delivered
            Thread.sleep(5000000);
            myClient.disconnect();
        } catch (MqttException e) {
            log.error("Exception when disconnecting MQTT connection", e);
        } catch (InterruptedException e) {
            log.error("Exception when listening for events", e);
        }
    }
}