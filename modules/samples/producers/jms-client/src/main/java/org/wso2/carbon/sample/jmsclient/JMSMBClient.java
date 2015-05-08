/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.databridge.commons.StreamDefinition;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JMSMBClient {

    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    String userName = "admin";
    String password = "admin";
    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String CARBON_DEFAULT_HOSTNAME = "localhost";
    private static String CARBON_DEFAULT_PORT = "5672";

    private static Logger log = Logger.getLogger(JMSMBClient.class);


    public void publishMessage(String format, String filePath, String topicName, StreamDefinition streamDefinition) throws NamingException, JMSException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));
        System.out.println("getTCPConnectionURL(userName,password) = " + getTCPConnectionURL(userName, password));
        InitialContext ctx = new InitialContext(properties);
        // Lookup connection factory

        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
        TopicConnection topicConnection = connFactory.createTopicConnection();
        topicConnection.start();

        TopicSession topicSession = topicConnection.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

        Topic topic = topicSession.createTopic(topicName);
        TopicPublisher topicPublisher = topicSession.createPublisher(topic);

        try {
            if(format.equalsIgnoreCase("csv")){
                String fileContent = JMSClientUtil.readCSVFile(filePath);
                log.info("Sending Map messages on '" + topicName + "' topic");
                publishMapMessage(topicPublisher, topicSession, fileContent, streamDefinition);

            }else{
                List<String> messagesList = JMSClientUtil.readFile(filePath);
                log.info("Sending  " + format + " messages on '" + topicName + "' topic");
                publishTextMessage(topicPublisher, topicSession, messagesList);
            }
            topicPublisher.close();
            topicSession.close();
            topicConnection.stop();

        } catch (JMSException e) {
            log.error("Can not subscribe." + e.getMessage());
        } catch (IOException e){
            log.error("Error when reading the data file." + e.getMessage());
        }


    }

    private static void publishMapMessage(TopicPublisher publisher, TopicSession topicSession, String messageContent, StreamDefinition streamDefinition) throws IOException, JMSException {
        final String META_DATA_PREFIX = "meta_";
        final String CORRELATION_DATA_PREFIX = "correlation_";
        List<Map<String, Object>> messages;
        if (streamDefinition == null) {
            // sending all attributes as string
            messages = JMSClientUtil.convertToMap(messageContent);
            for (int i = 0, mapMsgsLength = messages.size(); i < mapMsgsLength; i++) {
                Map<String, Object> message = messages.get(i);
                MapMessage mapMessage = topicSession.createMapMessage();
                for (Map.Entry<String, Object> entry : message.entrySet()) {
                    mapMessage.setObject(entry.getKey(), entry.getValue());
                }
                publisher.send(mapMessage);
                log.info("Map Message " + (i + 1) + " sent");
            }
        } else {
            // primitive typed attributes based on stream def.
            messages = JMSClientUtil.convertFileToMap(streamDefinition, messageContent);
            for (int i = 0, mapMsgsLength = messages.size(); i < mapMsgsLength; i++) {
                Map<String, Object> message = messages.get(i);
                MapMessage mapMessage = topicSession.createMapMessage();
                for (int j=0; j<streamDefinition.getMetaData().size(); j++){
                    String attributeName = streamDefinition.getMetaData().get(j).getName();
                    mapMessage.setObject(META_DATA_PREFIX + attributeName, message.get(attributeName));
                }
                for (int j=0; j<streamDefinition.getCorrelationData().size(); j++){
                    String attributeName = streamDefinition.getCorrelationData().get(j).getName();
                    mapMessage.setObject(CORRELATION_DATA_PREFIX + attributeName, message.get(attributeName));
                }
                for (int j=0; j<streamDefinition.getPayloadData().size(); j++){
                    String attributeName = streamDefinition.getPayloadData().get(j).getName();
                    mapMessage.setObject(attributeName, message.get(attributeName));
                }
                publisher.send(mapMessage);
                log.info("Map Message " + (i + 1) + " sent");
            }
        }

    }

    private static void publishTextMessage(MessageProducer producer, Session session, List<String> messagesList) throws JMSException {
        for(String message: messagesList){
            TextMessage jmsMessage = session.createTextMessage();
            jmsMessage.setText(message);
            producer.send(jmsMessage);
        }
    }

    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'")
                .toString();
    }
}
