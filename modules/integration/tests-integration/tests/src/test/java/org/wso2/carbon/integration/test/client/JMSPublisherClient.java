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
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JMS client reads a text file or a csv file with multiple messages and publish to a Map or Text message to ActiveMQ
 * message broker.
 */
public class JMSPublisherClient {

    private static Log log = LogFactory.getLog(JMSPublisherClient.class);

    /**
     * This method will publish the data in the test data file to the given topic via ActiveMQ message broker
     *
     * @param topicName             the topic which the messages should be published under
     * @param format                format of the test data file (csv or text)
     * @param testCaseFolderName    Testcase folder name which is in the test artifacts folder
     * @param dataFileName          data file name with the extension to be read
     *
     */
    public static void publish(String topicName, String format, String testCaseFolderName, String dataFileName) {

        if (format == null || "map".equals(format)) {
            format = "csv";
        }

        try{
            Properties properties = new Properties();

            String filePath = getTestDataFileLocation(testCaseFolderName, dataFileName);
            properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
            Context context = new InitialContext(properties);
            TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
            TopicConnection topicConnection = connFactory.createTopicConnection();
            topicConnection.start();
            Session session  = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic  = session.createTopic(topicName);
            MessageProducer producer  = session.createProducer(topic);

            List<String> messagesList = readFile(filePath);
            try {

                if(format.equalsIgnoreCase("csv")){
                    log.info("Sending Map messages on '" + topicName + "' topic");
                    publishMapMessage(producer, session, messagesList);

                }else{
                    log.info("Sending  " + format + " messages on '" + topicName + "' topic");
                    publishTextMessage(producer, session, messagesList);
                }
            } catch (IOException e) {
                log.error("Error when reading the data file." + e.getMessage(), e);
            } catch (JMSException e) {
                log.error("Can not subscribe." + e.getMessage(), e);
            } finally{
                producer.close();
                session.close();
                topicConnection.stop();
                topicConnection.close();
            }
        }catch(Exception e){
            log.error("Error when publishing messages" + e.getMessage(), e);
        }

        log.info("All Order Messages sent");
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer      Used for sending messages to a destination
     * @param session       Used to produce the messages to be sent
     * @param messagesList  List of messages to be send. An individual message event data should be in
     *                      "attributeName(attributeType):attributeValue" format
     *
     */
    public static void publishMapMessage(MessageProducer producer, Session session, List<String> messagesList) throws IOException, JMSException {
        String regexPattern = "(.*)\\((.*)\\):(.*)";
        Pattern pattern = Pattern.compile(regexPattern);
        for(String message: messagesList){
            MapMessage jmsMapMessage = session.createMapMessage();
            for (String line : message.split("\\n")) {
                if(line!=null && !line.equalsIgnoreCase("")){
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.find()){
                        jmsMapMessage.setObject(matcher.group(1), parseAttributeValue(matcher.group(2), matcher.group(3)));
                    }
                }
            }
            producer.send(jmsMapMessage);
        }
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer     Used for sending messages to a destination
     * @param session      Used to produce the messages to be sent
     * @param messagesList List of messages to be sent
     *
     */
    public static void publishTextMessage(MessageProducer producer, Session session, List<String> messagesList) throws JMSException {
        for(String message: messagesList){
            TextMessage jmsTextMessage = session.createTextMessage();
            jmsTextMessage.setText(message);
            producer.send(jmsTextMessage);
        }
    }

    private static Object parseAttributeValue(String type, String value) {
        switch (type){
            case "bool":
                return Boolean.parseBoolean(value);
            case "int":
                return Integer.parseInt(value);
            case "long":
                return Long.parseLong(value);
            case "float":
                return Float.parseFloat(value);
            case "double":
                return Double.parseDouble(value);
        }
        return value;
    }

    /**
     * Construct the data file location using the testcase folder name and the file name
     *
     * @param testCaseFolderName    Testcase folder name which is in the test artifacts folder
     * @param dataFileName          data file name with the extension to be read
     *
     */
    private static String getTestDataFileLocation(String testCaseFolderName, String dataFileName){
        String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() + CEPIntegrationTestConstants
                .RELATIVE_PATH_TO_TEST_ARTIFACTS + testCaseFolderName + File.separator + dataFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        return relativeFilePath;
    }

    /**
     * Read the file using the file path
     *
     * @param filePath    DataFile path to be read
     *
     */
    private static List<String> readFile(String filePath) {
        BufferedReader bufferedReader = null;
        StringBuffer message = new StringBuffer("");
        final String asterixLine = "*****";
        List<String> messagesList = new ArrayList<String>();
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

}
