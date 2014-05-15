package org.wso2.carbon.cep.sample;

import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class
        JNDIContext {
    private InitialContext initContext = null;
    private TopicConnectionFactory topicConnectionFactory = null;
    public static JNDIContext instance = null;

    private JNDIContext() {
        createInitialContext();
        createConnectionFactory();
    }

    public InitialContext getInitContext() {
        return initContext;
    }

    public TopicConnectionFactory getTopicConnectionFactory() {
        return topicConnectionFactory;
    }

    public static JNDIContext getInstance() {
        if (instance == null) {
            instance = new JNDIContext();
        }
        return instance;
    }

    /**
     * Create Connection factory with initial context
     */
    private void createConnectionFactory() {

        // create connection factory
        try {
            topicConnectionFactory = (TopicConnectionFactory) initContext.lookup("qpidConnectionfactory");
        } catch (NamingException e) {
            System.out.println("Can not create topic connection factory." + e);
        }
    }

    /**
     * Create Initial Context with given configuration
     */
    private void createInitialContext() {


        try {
            initContext = new InitialContext();
        } catch (NamingException e) {
            System.out.println("Can not create initial context with given parameters." + e);
        }
    }
}
