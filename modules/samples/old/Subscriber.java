package org.wso2.carbon.cep.sample;

import org.wso2.carbon.cep.sample.jms.JNDIContext;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Subscribe to myTopic and wait 10seconds to receive messages
 */
public class Subscriber implements MessageListener {
    private static InitialContext initContext = null;
    private static TopicConnectionFactory topicConnectionFactory = null;
    private static TopicConnection topicConnection = null;
    private boolean messageReceived = false;


    public Subscriber() {
        initContext = JNDIContext.getInstance().getInitContext();
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();
    }

    public void subscribe(String topicName) {


        Properties initialContextProperties = new Properties();
        initialContextProperties.put("java.naming.factory.initial",
                "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
        String connectionString = "amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5672'";
        initialContextProperties.put("connectionfactory.qpidConnectionfactory", connectionString);

        try {
            InitialContext initialContext = new InitialContext(initialContextProperties);
            TopicConnectionFactory topicConnectionFactory =
                    (TopicConnectionFactory) initialContext.lookup("qpidConnectionfactory");
            TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
            TopicSession topicSession =
                    topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = topicSession.createTopic(topicName);
            TopicSubscriber topicSubscriber =
                    topicSession.createSubscriber(topic);

            topicSubscriber.setMessageListener(this);
            topicConnection.start();
            System.out.println("Subscribed to topic :" + topic.getTopicName());


        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe() {

        try {
            if (topicConnection != null) {
                topicConnection.stop();
                topicConnection.close();
            }
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }
    }

    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println("output  = " + textMessage.getText());
                synchronized (this) {
                    messageReceived = true;
                }
            } catch (JMSException e) {
                System.out.println("error at getting text out of received message. = " + e);
            }
        } else {
            System.out.println("Received message is not a text message.");
        }
    }
}
