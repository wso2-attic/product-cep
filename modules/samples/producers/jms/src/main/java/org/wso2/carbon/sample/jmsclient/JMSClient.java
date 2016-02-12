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
package org.wso2.carbon.sample.jmsclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * JMS client reads a text file or a csv file with multiple messages and publish to a Map or Text message to a broker
 * (ActiveMQ, WSO2 Message Broker, Qpid Broker)
 */
public class JMSClient {

    private static Log log = LogFactory.getLog(JMSClient.class);

    public static void main(String[] args) {

        String sampleNumber = args[0];
        String topicName = args[1];
        String queueName = args[2];
        String format = args[3];
        String filePath = args[4];
        String broker = args[5];
        if (format == null || "map".equals(format)) {
            format = "csv";
        }
        if ("".equalsIgnoreCase(broker)) {
            broker = "activemq";
        }
        Session session = null;
        Properties properties = new Properties();
        if (!"activemq".equalsIgnoreCase(broker) && !"mb".equalsIgnoreCase(broker) && !"qpid".equalsIgnoreCase(broker)) {
            log.error("Please enter a valid JMS message broker. (ex: activemq, mb, qpid");
            return;
        }
        try {
            if (topicName != null && !"".equalsIgnoreCase(topicName)) {
                filePath = JMSClientUtil.getEventFilePath(sampleNumber, format, topicName, filePath);
                TopicConnection topicConnection;
                TopicConnectionFactory connFactory = null;
                if ("activemq".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
                } else if ("mb".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mb.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                } else if ("qpid".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("qpid.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                }
                if (connFactory != null) {
                    topicConnection = connFactory.createTopicConnection();
                    topicConnection.start();
                    session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                    if (session != null) {
                        Topic topic = session.createTopic(topicName);
                        MessageProducer producer = session.createProducer(topic);
                        List<String> messagesList = JMSClientUtil.readFile(filePath);
                        try {
                            if ("csv".equalsIgnoreCase(format)) {
                                log.info("Sending Map messages on '" + topicName + "' topic");
                                JMSClientUtil.publishMapMessage(producer, session, messagesList);

                            } else {
                                log.info("Sending  " + format + " messages on '" + topicName + "' topic");
                                JMSClientUtil.publishTextMessage(producer, session, messagesList);
                            }
                            log.info("All Order Messages sent");
                        } catch (JMSException e) {
                            log.error("Cannot subscribe." + e.getMessage(), e);
                        } catch (IOException e) {
                            log.error("Error when reading the data file." + e.getMessage(), e);
                        } finally {
                            producer.close();
                            session.close();
                            topicConnection.stop();
                        }
                    }
                } else {
                    log.error("Error when creating connection factory. Please check necessary jar files");
                }
            } else if (queueName != null && !queueName.equalsIgnoreCase("")) {
                filePath = JMSClientUtil.getEventFilePath(sampleNumber, format, queueName, filePath);
                QueueConnection queueConnection;
                QueueConnectionFactory connFactory = null;
                if ("activemq".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("activemq.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (QueueConnectionFactory) context.lookup("ConnectionFactory");
                } else if ("mb".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mb.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (QueueConnectionFactory) context.lookup("qpidConnectionFactory");
                } else if ("qpid".equalsIgnoreCase(broker)) {
                    properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("qpid.properties"));
                    Context context = new InitialContext(properties);
                    connFactory = (QueueConnectionFactory) context.lookup("qpidConnectionFactory");
                }
                if (connFactory != null) {
                    queueConnection = connFactory.createQueueConnection();
                    queueConnection.start();
                    session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                    if (session != null) {
                        Queue queue = session.createQueue(queueName);
                        MessageProducer producer = session.createProducer(queue);
                        List<String> messagesList = JMSClientUtil.readFile(filePath);
                        try {
                            if ("csv".equalsIgnoreCase(format)) {
                                log.info("Sending Map messages on '" + queueName + "' queue");
                                JMSClientUtil.publishMapMessage(producer, session, messagesList);

                            } else {
                                log.info("Sending  " + format + " messages on '" + queueName + "' queue");
                                JMSClientUtil.publishTextMessage(producer, session, messagesList);
                            }
                        } catch (JMSException e) {
                            log.error("Cannot subscribe." + e.getMessage(), e);
                        } catch (IOException e) {
                            log.error("Error when reading the data file." + e.getMessage(), e);
                        } finally {
                            producer.close();
                            session.close();
                            queueConnection.stop();
                        }
                    }
                } else {
                    log.error("Error when creating connection factory. Please check necessary jar files");
                }
            } else {
                log.error("Enter queue name or topic name to be published!");
            }
        } catch (Exception e) {
            log.error("Error when publishing" + e.getMessage(), e);
        }
    }
}