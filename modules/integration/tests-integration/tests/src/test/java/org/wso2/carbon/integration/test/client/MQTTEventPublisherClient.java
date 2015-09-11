/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * MQTTEventPublisherClient client reads a text file with multiple events in different formats and post it to the given url.
 */
public class MQTTEventPublisherClient {
    private static Log log = LogFactory.getLog(MQTTEventPublisherClient.class);

    public static void publish(String url, String topic, String testCaseFolderName, String dataFileName) {
        log.info("Starting MQTT EventPublisher Client");
        KeyStoreUtil.setTrustStoreParams();
        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);
        MqttClient mqttClient = null;
        try {
            mqttClient = new MqttClient(url, "SIMPLE-MQTT-PUB");
            mqttClient.connect(connOpt);

            List<String> messagesList = readMsg(getTestDataFileLocation(testCaseFolderName, dataFileName));
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
            if (mqttClient != null) {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    log.error("Error while disconnecting the MQTT client", e);
                }
            }
        }
    }

    /**
     * Messages will be read from the given filepath and an ArrayList will be returned (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static List<String> readMsg(String filePath) {
        List<String> messagesList = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        StringBuffer message = new StringBuffer("");
        final String asterixLine = "*****";
        try {
            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(asterixLine.trim()) && !"".equals(message.toString().trim()))) {
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
        return messagesList;
    }

    /**
     * File path will be created for the file to be read with respect to the artifact folder and file name
     *
     * @param testCaseFolderName Artifact folder name
     * @param dataFileName       Text file to be read
     */
    public static String getTestDataFileLocation(String testCaseFolderName, String dataFileName) throws Exception {
        String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                                  CEPIntegrationTestConstants.RELATIVE_PATH_TO_TEST_ARTIFACTS +
                                  testCaseFolderName + File.separator + dataFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        return relativeFilePath;
    }

}

