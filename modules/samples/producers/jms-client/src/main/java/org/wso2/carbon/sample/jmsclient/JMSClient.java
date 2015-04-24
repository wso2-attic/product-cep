/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JMSClient {

    private static Logger log = Logger.getLogger(JMSClient.class);

    private static TopicConnectionFactory topicConnectionFactory = null;

    public static void main(String[] args) {
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();

        String sampleNumber = args[0];
        String topic = args[1];
        String format = args[2];
        String filePath = args[3];
        String streamId = args[4];

        if (format == null || "map".equals(format)) {
            format = "csv";
        }

        JMSClient publisher = new JMSClient();

        StreamDefinition streamDefinition = null;
        if (streamId != null && streamId.length() > 0) {
            streamDefinition = JMSClientUtil.loadStreamDefinitions(sampleNumber).get(streamId);
        }
        try {
            filePath = JMSClientUtil.getEventFilePath(sampleNumber, topic, filePath);
            String fileContent = JMSClientUtil.readFile(filePath + "." + format);
            publisher.publish(topic, fileContent, format, streamDefinition);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("All Order Messages sent");
    }

    /**
     * Publish message to given topic
     */
    public void publish(String topicName, String fileContent, String format, StreamDefinition streamDefinition) {
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            log.error("Can not create topic connection." + e);
            return;
        }
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);

            if ("csv".equals(format)) {
                log.info("Sending Map messages on '" + topicName + "' topic");
                publishMapMessage(producer, session, fileContent, streamDefinition);
            } else if ("text".equals(format) || "txt".equals(format) || "json".equals(format) || "xml".equals(format)) {
                log.info("Sending  " + format + " messages on '" + topicName + "' topic");
                publishTextMessage(producer, session, fileContent);
            }
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            log.error("Can not subscribe." + e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void publishMapMessage(MessageProducer producer, Session session, String messageContent, StreamDefinition streamDefinition) throws IOException, JMSException {

        List<Map<String, Object>> messages;
        if (streamDefinition == null) {
            // sending all attributes as string
            messages = JMSClientUtil.convertToMap(messageContent);
        } else {
            // primitive typed attributes based on stream def.
            messages = JMSClientUtil.convertFileToMap(streamDefinition, messageContent);
        }
        for (int i = 0, mapMsgsLength = messages.size(); i < mapMsgsLength; i++) {
            Map<String, Object> message = messages.get(i);
            MapMessage mapMessage = session.createMapMessage();
            for (Map.Entry<String, Object> entry : message.entrySet()) {
                mapMessage.setObject(entry.getKey(), entry.getValue());
            }
            producer.send(mapMessage);
            log.info("Map Message " + (i + 1) + " sent");
        }
    }

    private static void publishTextMessage(MessageProducer producer, Session session, String messageContent) throws JMSException {
        TextMessage jmsMessage = session.createTextMessage();
        jmsMessage.setText(messageContent);
        producer.send(jmsMessage);

    }

}
