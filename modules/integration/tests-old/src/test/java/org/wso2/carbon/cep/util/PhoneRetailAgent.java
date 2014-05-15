package org.wso2.carbon.cep.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;


import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Random;

public class PhoneRetailAgent {
    private static Logger logger = Logger.getLogger(PhoneRetailAgent.class);
    public static final String PHONE_RETAIL_STREAM = "org.wso2.phone.retail.store";
    public static final String VERSION = "1.2.0";

    public static final String[] phoneModels = {"Nokia", "Apple", "Samsung", "Sony-Ericson", "LG"};
    public static final String[] buyer = {"James", "Mary", "John", "Peter", "Harry", "Tom", "Paul"};
    public static final int[] quantity = {2, 5, 3, 4, 1};
    public static final int[] price = {500, 350, 900, 800, 700};


    public static void publish() throws AgentException,
                                        MalformedStreamDefinitionException,
                                        StreamDefinitionException,
                                        DifferentStreamDefinitionAlreadyDefinedException,
                                        MalformedURLException,
                                        AuthenticationException,
                                        NoStreamDefinitionExistException,
                                        TransportException, SocketException,
                                        org.wso2.carbon.databridge.commons.exception.AuthenticationException {
        String host = "localhost";
        int port = 7611;
        int events = 20;


        logger.info("Starting Phone Retail Shop Agent");

        KeyStoreUtil.setTrustStoreParams();

        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, "admin", "admin", agent);
        try {

            String streamId = null;
            try {
                streamId = dataPublisher.findStream(PHONE_RETAIL_STREAM, VERSION);
                logger.info("Stream already defined");

            } catch (NoStreamDefinitionExistException e) {
                streamId = dataPublisher.defineStream("{" +
                                                      "  'name':'" + PHONE_RETAIL_STREAM + "'," +
                                                      "  'version':'" + VERSION + "'," +
                                                      "  'nickName': 'Phone_Retail_Shop'," +
                                                      "  'description': 'Phone Sales'," +
                                                      "  'metaData':[" +
                                                      "          {'name':'clientType','type':'STRING'}" +
                                                      "  ]," +
                                                      "  'payloadData':[" +
                                                      "          {'name':'brand','type':'STRING'}," +
                                                      "          {'name':'quantity','type':'INT'}," +
                                                      "          {'name':'total','type':'INT'}," +
                                                      "          {'name':'buyer','type':'STRING'}" +
                                                      "  ]" +
                                                      "}");
            }


            //Publish event for a valid stream
            if (!streamId.isEmpty()) {
                logger.info("Stream ID: " + streamId);

                for (int i = 0; i < events; i++) {
                    publishEvents(dataPublisher, streamId, i);
                    logger.info("Events published : " + (i + 1));
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        } finally {
            dataPublisher.stop();
        }

    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int i)
            throws AgentException {
        int quantity = getRandomQuantity();
        Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                                   new Object[]{getRandomProduct(), quantity, quantity * getRandomPrice(), getRandomUser()});
        logger.info(eventOne);
        dataPublisher.publish(eventOne);
    }

    private static String getRandomProduct() {
        return phoneModels[getRandomId(5)];
    }

    private static String getRandomUser() {
        return buyer[getRandomId(7)];
    }

    private static int getRandomQuantity() {
        return quantity[getRandomId(5)];
    }

    private static int getRandomPrice() {
        return price[getRandomId(5)];
    }


    private static int getRandomId(int i) {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
    }
}
