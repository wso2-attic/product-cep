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

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.*;

/**
 * JMS consumer client subscribes to an ActiveMQ broker and retrieves the messages with a certain topic name or a queue name
 */
public class JMSConsumerClient implements Runnable{

    private static Log log = LogFactory.getLog(JMSConsumerClient.class);
    private static TopicConnectionFactory topicConnectionFactory = null;
    private static String topicName = null;
    private static boolean active = true;
    private static int messageCount = 0;
    private static List<Object> preservedEventList = null;
    /**
     * This method will start the jms subscriber
     *
     * @param topic the topic which the consumer subscribe with the ActiveMQ broker
     *
     */
    public static void startConsumer(String topic) throws InterruptedException {
        messageCount = 0;
        active = true;
        Properties properties = new Properties();
        topicName = topic;
        preservedEventList = new ArrayList<>();
        try {
            properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
            Context context = new InitialContext(properties);
            topicConnectionFactory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
            JMSConsumerClient topicConsumer = new JMSConsumerClient();
            Thread consumerThread = new Thread(topicConsumer);
            log.info("Starting ActiveMQ consumerTopic thread...");
            consumerThread.start();
        } catch (IOException e) {
            log.error("Cannot read properties file from resources. " + e.getMessage(), e);
        } catch (NamingException e) {
            log.error("Invalid properties in the properties " + e.getMessage(), e);
        }
    }

    public void run() {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            log.error("Can not create topic connection." + e.getMessage(), e);
            return;
        }
        Session session = null;
        try {

            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(destination);
            log.info("Listening for messages");
            while (active) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    messageCount++;
                    if (message instanceof MapMessage) {
                        MapMessage mapMessage=(MapMessage)message;
                        Map<String, Object> map = new HashMap<String, Object>();
                        Enumeration enumeration = mapMessage.getMapNames();
                        while (enumeration.hasMoreElements()) {
                            String key = (String) enumeration.nextElement();
                            map.put(key, mapMessage.getObject(key));
                        }
                        preservedEventList.add(map);
                        log.info("Received Map Message : \n" + map + "\n");
                    } else if(message instanceof TextMessage) {
                        String textMessage = ((TextMessage) message).getText();
                        preservedEventList.add(textMessage);
                        log.info("Received Text Message : \n" + textMessage + "\n");
                    } else {
                        preservedEventList.add(message.toString());
                        log.info("Received message : \n" + message.toString() + "\n");
                    }
                }
            }
            log.info("Finished listening for messages.");
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            log.error("Can not subscribe." + e.getMessage(), e);
        }
    }
    public static void shutdown() {
        active = false;
        log.info("Shutting down ActiveMQ consumerTopic thread...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }
    public static int getMessageCount(){
        return messageCount;
    }

    public static List<Object> getPreservedEventList(){
        return preservedEventList;
    }

}
