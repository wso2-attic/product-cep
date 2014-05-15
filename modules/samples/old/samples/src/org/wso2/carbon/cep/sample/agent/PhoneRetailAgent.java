package org.wso2.carbon.cep.sample.agent;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Random;

public class PhoneRetailAgent {
    private static Logger logger = Logger.getLogger(PhoneRetailAgent.class);
    public static final String ANALYTICS_STATISTICS_STREAM = "analytics_Statistics";
    public static final String VERSION = "1.3.0";

    public static void main(String[] args) throws AgentException,
                                                  MalformedStreamDefinitionException,
                                                  StreamDefinitionException,
                                                  DifferentStreamDefinitionAlreadyDefinedException,
                                                  MalformedURLException,
                                                  AuthenticationException,
                                                  NoStreamDefinitionExistException,
                                                  TransportException, SocketException,
                                                  org.wso2.carbon.databridge.commons.exception.AuthenticationException {
        String host="localhost";
        int port=7611;

        System.out.println("Starting Phone Retail Shop Agent");

        KeyStoreUtil.setTrustStoreParams();

        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://"+host+":"+port, "admin", "admin", agent);
        String streamId = null;

        try {
            streamId = dataPublisher.findStream(ANALYTICS_STATISTICS_STREAM, VERSION);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'analytics_Statistics'," +
                                                  "  'version':'1.3.0'," +
                                                  "  'nickName': 'Analytics Statistics Information'," +
                                                  "  'description': 'Details of Analytics Statistics'," +
                                                  "  'metaData':[" +
                                                  "          {'name':'ipAdd','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  "          {'name':'userID','type':'STRING'}," +
                                                  "          {'name':'searchTerms','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");

        }


        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            Thread.sleep(1000);
            //In this case correlation data is null
            dataPublisher.publish(streamId, new Object[]{"192.168.1.1"}, null, new Object[]{"abc@org1.com", "CEP"});
            dataPublisher.publish(streamId, new Object[]{"192.168.1.1"}, null, new Object[]{"anne@org2.com", "CEP"});
            dataPublisher.publish(streamId, new Object[]{"192.168.1.3"}, null, new Object[]{"sam@org1.com", "CEP"});
            dataPublisher.publish(streamId, new Object[]{"192.168.1.2"}, null, new Object[]{"anne@org1.com", "CEP"});
            dataPublisher.publish(streamId, new Object[]{"192.168.1.3"}, null, new Object[]{"ann@org3.com", "CEP"});

            Thread.sleep(3000);
            dataPublisher.stop();
        }
    }



}
