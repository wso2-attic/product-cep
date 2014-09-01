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
package org.wso2.carbon.sample.purchaseinfo;

import javax.jms.*;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapPurchaseInfoClient {

    private static final List<Map<String,Object>> jsonMsgs = new ArrayList ();

    static {
        Map<String,Object> tempMap = new HashMap<String, Object>();
        tempMap.put("cardId", "1234-3244-2432-4124");
        tempMap.put("amount", 100.00);
        tempMap.put("countryId", "AU");
        jsonMsgs.add(tempMap);

        Map<String,Object>   tempMap1 = new HashMap<String, Object>();
        tempMap1.put("cardId", "1234-3244-2432-0124");
        tempMap1.put("amount", 600.00);
        tempMap1.put("countryId", "UK");
        jsonMsgs.add(tempMap1);

        Map<String,Object> tempMap2 = new HashMap<String, Object>();
        tempMap2.put("cardId", "1234-3244-2432-7124");
        tempMap2.put("amount", 10045.00);
        tempMap2.put("countryId", "AU");
        jsonMsgs.add(tempMap2);

        Map<String,Object> tempMap3 = new HashMap<String, Object>();
        tempMap3.put("cardId", "1234-3244-2432-1124");
        tempMap3.put("amount", 70.50);
        tempMap3.put("countryId", "US");
        jsonMsgs.add(tempMap3);

        Map<String,Object> tempMap4 = new HashMap<String, Object>();
        tempMap4.put("cardId", "1234-3244-2432-9124");
        tempMap4.put("amount", 35.00);
        tempMap4.put("countryId", "AU");
        jsonMsgs.add(tempMap4);

        Map<String,Object> tempMap5 = new HashMap<String, Object>();
        tempMap5.put("cardId", "1234-3244-2432-4124");
        tempMap5.put("amount", 1500.00);
        tempMap5.put("countryId", "UK");
        jsonMsgs.add(tempMap5);

        Map<String,Object> tempMap6 = new HashMap<String, Object>();
        tempMap6.put("cardId", "1234-3244-2432-1124");
        tempMap6.put("amount", 1060.00);
        tempMap6.put("countryId", "US");
        jsonMsgs.add(tempMap6);

    }


    private static TopicConnectionFactory topicConnectionFactory = null;

    public static void main(String[] args)
            throws InterruptedException, NamingException {
//        Properties initialContextProperties = new Properties();
//        initialContextProperties.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
//        initContext = new InitialContext(initialContextProperties);
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();
        MapPurchaseInfoClient publisher = new MapPurchaseInfoClient();
        String topicName = args[0];
        Thread.sleep(2000);

        publisher.publish(topicName, jsonMsgs);


        System.out.println("All Order Messages sent");

    }

    /**
     * Publish message to given topic
     *
     * @param topicName - topic name to publish messages
     * @param messages  - messages to send
     */
    public void publish(String topicName, List<Map<String, Object>> messages) {
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
            System.out.println("Sending Map messages on '" + topicName + "' topic");
            for (int i = 0, jsonMsgsLength = messages.size(); i < jsonMsgsLength; i++) {
                Map<String, Object> message = messages.get(i);
                MapMessage mapMessage = session.createMapMessage();
                for(Map.Entry<String,Object> entry:message.entrySet()){
                    mapMessage.setObject(entry.getKey(),entry.getValue());
                }
                producer.send(mapMessage);
                System.out.println("Order Message " + (i + 1) + " sent");
            }
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }
}