/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cep.sample.jms;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Publish message to the topic created when defining Input
 */
public class AllStockQuotesPublisher {
    private static InitialContext initContext = null;
    private static TopicConnectionFactory topicConnectionFactory = null;

    public static void main(String[] args)
            throws XMLStreamException, InterruptedException, NamingException {

        Properties initialContextProperties = new Properties();
        initialContextProperties.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        initContext = new InitialContext(initialContextProperties);
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();

        AllStockQuotesPublisher publisher = new AllStockQuotesPublisher();

        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("symbol", "MSFT");
        map1.put("price", 26.36);
        publisher.publish("AllStockQuotes", map1);
        System.out.println("AllStockQuotes Message 1 sent");
        Thread.sleep(2000);

        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("symbol", "MSFT");
        map2.put("price", 36.36);
        publisher.publish("AllStockQuotes", map2);
        System.out.println("AllStockQuotes Message 2 sent");
        Thread.sleep(2000);

        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("symbol", "MSFT");
        map3.put("price", 6.36);
        publisher.publish("AllStockQuotes", map3);
        System.out.println("AllStockQuotes Message 3 sent");

    }

    /**
     * Publish message to given topic
     *
     * @param topicName - topic name to publish messages
     * @param message   - message to send
     */
    public void publish(String topicName, Map<String, Object> message) {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }

        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            MapMessage jmsMessage = session.createMapMessage();
            for (Map.Entry<String, Object> entry : message.entrySet()) {
                if (entry.getValue() instanceof Double) {
                    jmsMessage.setDouble(entry.getKey(), (Double) entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                    jmsMessage.setInt(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    jmsMessage.setLong(entry.getKey(), (Long) entry.getValue());
                } else if (entry.getValue() instanceof Float) {
                    jmsMessage.setFloat(entry.getKey(), (Float) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    jmsMessage.setString(entry.getKey(), (String) entry.getValue());
                }
            }
            producer.send(jmsMessage);
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }
}