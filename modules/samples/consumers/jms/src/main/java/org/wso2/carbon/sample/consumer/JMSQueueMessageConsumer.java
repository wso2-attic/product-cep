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
package org.wso2.carbon.sample.consumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JMSQueueMessageConsumer implements Runnable {
    private static QueueConnectionFactory queueConnectionFactory = null;
    private String queueName = "";
    private boolean active = true;

    JMSQueueMessageConsumer(String queueName) {
        this.queueName = queueName;
    }

    public static void main(String[] args)
            throws InterruptedException, NamingException {

        queueConnectionFactory = JNDIContext.getInstance().getQueueConnectionFactory();

        String queueName = "";
        if (args.length == 0 || args[0] == null || args[0].trim().equals("")) {
            queueName = "DelayedFlightStats";
        } else {
            queueName = args[0];
        }
        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(queueName);
        Thread consumerThread = new Thread(consumer);
        System.out.println("Starting consumer thread...");
        consumerThread.start();
        Thread.sleep(5 * 60000);
        System.out.println("Shutting down consumer...");
        consumer.shutdown();
    }

    public void shutdown() {
        active = false;
    }

    public void run() {
        // create queue connection
        QueueConnection queueConnection = null;
        try {
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create queue connection." + e);
            return;
        }
        Session session = null;
        try {

            session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);
            System.out.println("Listening for messages");
            while (active) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    if (message instanceof MapMessage) {
                        MapMessage mapMessage = (MapMessage) message;
                        Map<String, Object> map = new HashMap<String, Object>();
                        Enumeration enumeration = mapMessage.getMapNames();
                        while (enumeration.hasMoreElements()) {
                            String key = (String) enumeration.nextElement();
                            map.put(key, mapMessage.getObject(key));
                        }
                        System.out.println("Received Map Message : " + map);
                    } else if (message instanceof TextMessage) {
                        System.out.println("Received Text Message : " + ((TextMessage) message).getText());
                    } else {
                        System.out.println("Received message : " + message.toString());
                    }
                }
            }
            System.out.println("Finished listening for messages.");
            session.close();
            queueConnection.stop();
            queueConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }
}
