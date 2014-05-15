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
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.String;
import java.util.Enumeration;

/**
 * Subscribe to myTopic and wait 10seconds to receive messages
 */
public class JMSSubscriber implements MessageListener {
    private static InitialContext initContext = null;
    private static TopicConnectionFactory topicConnectionFactory = null;
    private boolean messageReceived = false;
    private static String topicName = null;

    public static void main(String[] args) throws NamingException {

        if (args.length != 0 && args[0] != null) {
            topicName = args[0];
        }
        new JMSSubscriber().subscribe(topicName);
    }

    public void subscribe(String topicName) throws NamingException {

        System.out.println("Subscribing to : "+topicName);
        initContext = JNDIContext.getInstance().getInitContext();
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();

        // create connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }
        // create session, subscriber, message listener and listen on that topic
        TopicSession session = null;
        try {
            session = topicConnection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            // change topic name to brokerConfiguration name + topic name
            Topic topic = session.createTopic(topicName);
            TopicSubscriber subscriber = session.createSubscriber(topic);
            subscriber.setMessageListener(this);
            topicConnection.start();
            synchronized (this) {
                while (!messageReceived) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }

    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println("output = " + textMessage.getText());
                synchronized (this) {
                    messageReceived = true;
                }
            } catch (JMSException e) {
                System.out.println("error at getting text out of received message. = " + e);
            }
        } else if (message instanceof MapMessage) {
            try {
                Enumeration enumeration = ((MapMessage) message).getMapNames();
                for (; enumeration.hasMoreElements(); ) {
                    System.out.println(((MapMessage) message).getString((String) enumeration.nextElement()));
                }
                System.out.println();
            } catch (JMSException e) {
                System.out.println("error at getting element out of received map message. = " + e);
            }
        } else {
            System.out.println("Received message is not a text/map message.");
        }
    }
}

