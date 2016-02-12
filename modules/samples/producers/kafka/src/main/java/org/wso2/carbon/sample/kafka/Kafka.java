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

package org.wso2.carbon.sample.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Kafka client reads a text file with multiple xml messages and post it to the given url.
 */
public class Kafka {
    private static Log log = LogFactory.getLog(Kafka.class);
    private static List<String> messagesList = new ArrayList<String>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String asteriskLine = "*****";

    public static void main(String args[]) {
        log.info("Command line arguments passed: " + Arrays.deepToString(args));
        log.info("Starting Kafka Client");

        String url = args[0];
        String topic = args[1];
        String filePath = args[2];
        String sampleNumber = args[3];

        Properties props = new Properties();
        props.put("metadata.broker.list", url);
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, Object> producer = new Producer<String, Object>(config);

        try {

            filePath = KafkaUtil.getEventFilePath(sampleNumber, topic, filePath);
            readMsg(filePath);

            for (String message : messagesList) {
                System.out.println("Sending message:");
                System.out.println(message);
                KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic, message);
                producer.send(data);
            }
            Thread.sleep(500);

        } catch (Throwable t) {
            log.error("Error when sending the messages", t);
        } finally {
            producer.close();
        }
    }

    /**
     * Xml messages will be read from the given filePath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static void readMsg(String filePath) {

        try {

            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(asteriskLine.trim()) && !"".equals(message.toString().trim()))) {
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

