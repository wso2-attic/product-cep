/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.sample.consumer;


import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TopicConsumer implements Runnable {

    private TopicConnectionFactory topicConnectionFactory;
    private String topicName;
    private boolean active = true;
    private static Logger log = Logger.getLogger(TopicConsumer.class);

    public TopicConsumer(TopicConnectionFactory topicConnectionFactory, String topicName){
        this.topicConnectionFactory = topicConnectionFactory;
        this.topicName = topicName;
    }
    public void run() {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            log.error("Can not create topic connection." + e.getMessage());
            return;
        }
        Session session = null;
        try {

            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(destination);
            System.out.println("Listening for messages");
            while (active) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    if (message instanceof MapMessage) {
                        MapMessage mapMessage=(MapMessage)message;
                        Map<String, Object> map = new HashMap<String, Object>();
                        Enumeration enumeration = mapMessage.getMapNames();
                        while (enumeration.hasMoreElements()) {
                            String key = (String) enumeration.nextElement();
                            map.put(key, mapMessage.getObject(key));
                        }
                        System.out.println("Received Map Message : " + map);
                    } else if(message instanceof TextMessage) {
                        System.out.println("Received Text Message : " + ((TextMessage)message).getText());
                    } else {
                        System.out.println("Received message : " + message.toString());
                    }
                }
            }
            System.out.println("Finished listening for messages.");
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }
    public void shutdown() {
        active = false;
    }
}
