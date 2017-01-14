/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.test.client;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * KafkaEventPublisherClient client reads a text file with multiple events and publish it to the given url.
 */
public class KafkaEventPublisherClient {
    private static Log log = LogFactory.getLog(KafkaEventPublisherClient.class);

    public static void publish(String url, String topic, String testCaseFolderName, String dataFileName) {
        log.info("Starting Kafka EventPublisher Client");

        Properties props = new Properties();
        props.put("metadata.broker.list", url);
        props.put("producer.type", "sync");
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, Object> producer = new Producer<String, Object>(config);

        try {
            List<String> messagesList = readMsg(getTestDataFileLocation(testCaseFolderName, dataFileName));
            for (String message : messagesList) {
                log.info(String.format("Sending message: %s", message));
                KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic, message);
                producer.send(data);
                Thread.sleep(100);
            }
            Thread.sleep(1000);
        } catch (Throwable t) {
            log.error("Error when sending the messages", t);
        } finally {
            producer.close();
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

