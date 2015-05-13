/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.log4j.Logger;

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

    private static Logger log = Logger.getLogger(JMSClient.class);

    public static void main(String[] args) {

        String sampleNumber = args[0];
        String topicName = args[1];
        String format = args[2];
        String filePath = args[3];
        String broker = args[4];

        if (format == null || "map".equals(format)) {
            format = "csv";
        }
        if (broker == null || broker.equalsIgnoreCase("")) {
            broker = "activemq";
        }

        try {

            filePath = JMSClientUtil.getEventFilePath(sampleNumber, format, topicName, filePath);

            TopicConnection topicConnection = null;
            Session session = null;

            Properties properties = new Properties();
            boolean sessionAutoAcknowledge = true;
            if(broker.equalsIgnoreCase("activemq")){
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("jndi.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            }else if(broker.equalsIgnoreCase("mb")){
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mbJms.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            }else if(broker.equalsIgnoreCase("qpid")){
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("qpid.properties"));
                Context context = new InitialContext(properties);
                TopicConnectionFactory connFactory = (TopicConnectionFactory) context.lookup("qpidConnectionFactory");
                topicConnection = connFactory.createTopicConnection();
                topicConnection.start();
                session = topicConnection.createSession(true, Session.SESSION_TRANSACTED);
                sessionAutoAcknowledge = false;
            }else{
                log.info("Please enter a valid JMS message broker. (ex: activemq, mb, qpid");
            }

            if(session != null){
                Topic topic  = session.createTopic(topicName);
                MessageProducer producer  = session.createProducer(topic);

                List<String> messagesList = JMSClientUtil.readFile(filePath);
                try {
                    if(format.equalsIgnoreCase("csv")){
                        log.info("Sending Map messages on '" + topicName + "' topic");
                        JMSClientUtil.publishMapMessage(producer, session, messagesList, sessionAutoAcknowledge);

                    }else{
                        log.info("Sending  " + format + " messages on '" + topicName + "' topic");
                        JMSClientUtil.publishTextMessage(producer, session, messagesList, sessionAutoAcknowledge);
                    }
                    producer.close();
                    session.close();
                    topicConnection.stop();

                } catch (JMSException e) {
                    log.error("Can not subscribe." + e.getMessage());
                } catch (IOException e){
                    log.error("Error when reading the data file." + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("logging error" + e.getMessage());
        }
        log.info("All Order Messages sent");
    }
}
