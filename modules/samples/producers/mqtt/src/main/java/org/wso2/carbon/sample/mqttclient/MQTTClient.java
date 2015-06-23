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

package org.wso2.carbon.sample.mqttclient;


import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MQTTClient {

    private static Logger log = Logger.getLogger(MQTTClient.class);
    private static List<String> messagesList = new ArrayList<>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String messageEndLine = "*****";


    public static void main(String args[]) {
        String url = args[0];
        String topic = args[1];
        String filePath = args[2];
        String sampleNumber = null;
        if (args.length == 4) {
            sampleNumber = args[3];
        }

        log.info("Starting MQTT Client");
        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);

        try {
            MqttClient mqttClient = new MqttClient(url, "SIMPLE-MQTT-PUB");
            mqttClient.connect(connOpt);

            try {
                filePath = MQTTClientUtil.getMessageFilePath(sampleNumber, filePath, topic);
                readMsg(filePath);

                for (String message : messagesList) {
                    log.info("Sending message:");
                    log.info(message);
                    int pubQoS = 1;
                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(pubQoS);
                    mqttClient.publish(topic, mqttMessage);
                }
                Thread.sleep(500); // Waiting time for the message to be sent

            } catch (Throwable t) {
                log.error("Error when sending the messages", t);
            } finally {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    log.error("Error while disconnecting the MQTT client", e);
                }
            }

        } catch (MqttException e) {
            log.error("Error while connecting to MQTT server", e);
        }
    }

    /**
     * Xml messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static void readMsg(String filePath) {

        try {

            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(messageEndLine.trim()) && !"".equals(message.toString().trim()))) {
                    messagesList.add(message.toString());
                    message = new StringBuffer("");
                } else {
                    message = message.append(String.format("\n%s", line));
                }
            }
            if (!"".equals(message.toString().trim())) {
                messagesList.add(message.toString());
            }

        } catch (IOException e) {
            log.error("Error in reading file " + filePath, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when closing the file : " + e.getMessage(), e);
            }
        }

    }


}
