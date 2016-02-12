/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.sample.jmsclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.util.*;

/**
 * JMS client reads a text file or a csv file with multiple messages and publish to a Map or Text message to a broker
 * (ActiveMQ, WSO2 Message Broker, Qpid Broker)
 */
public class JMSClient {

    static int threads = 4;
    static int sessionsPerThread = 5;
    static int msgsPerSession = 20000;

    private static Log log = LogFactory.getLog(JMSClient.class);

    public static void main(String[] args) {

        final String topicName = args[0];
        String broker0 = args[1];

        if (broker0 == null || broker0.equalsIgnoreCase("")) {
            broker0 = "activemq";
        }

        final String broker = broker0;

        for (int i = 0; i < threads; i++) {
            new Thread() {
                public void run() {
                    publishMessages(topicName,  broker);

                }
            }.start();
        }

        log.info("All Messages sent");
    }

    public static void publishMessages(String topicName, String broker) {
        try {

//            filePath = JMSClientUtil.getEventFilePath(sampleNumber, format, topicName, filePath);

            TopicConnection topicConnection = null;
            Session session = null;

            Properties properties = new Properties();
            if (broker.equalsIgnoreCase("activemq")) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            } else if (broker.equalsIgnoreCase("mb")) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mb.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            } else if (broker.equalsIgnoreCase("qpid")) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("qpid.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            } else {
                log.info("Please enter a valid JMS message broker. (ex: activemq, mb, qpid");
            }

            if (session != null) {
                Topic topic = session.createTopic(topicName);
                MessageProducer producer = session.createProducer(topic);

                try {

                        List<Map<String, Object>> messageList = new ArrayList<Map<String, Object>>();

                        Random random = new Random();
                        for (int j = 0; j < sessionsPerThread; j++) {
                            for (int i = 0; i < msgsPerSession; i++) {
                                HashMap<String, Object> map = new HashMap<String, Object>();

                                map.put("id", random.nextInt() +"");
                                map.put("value", random.nextInt());
                                map.put("content", "sample content");
                                map.put("client", "jmsQueueClient");
                                // setting the timestamp later
                                messageList.add(map);
                            }

                            publishMapMessage(producer, session, messageList);

                        }
                } catch (JMSException e) {
                    log.error("Can not subscribe." + e.getMessage(), e);
                } catch (IOException e) {
                    log.error("Error when reading the data file." + e.getMessage(), e);
                } finally {
                    producer.close();
                    session.close();
                    topicConnection.stop();
                }
            }
        } catch (Exception e) {
            log.error("Error when publishing message" + e.getMessage(), e);
        }
    }

    public static void publishMapMessage(MessageProducer producer, Session session, List<Map<String, Object>> messagesList)
            throws IOException, JMSException {
        for (Map<String, Object> message : messagesList) {
            MapMessage mapMessage = session.createMapMessage();
            message.put("time", System.currentTimeMillis());
            for (Map.Entry<String, Object> entry : message.entrySet()) {
                mapMessage.setObject(entry.getKey(), entry.getValue());
            }
            producer.send(mapMessage);
        }
        log.info("messages sent (per-thread):" + messagesList.size());
    }

}
