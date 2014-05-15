package org.wso2.carbon.cep.sample.client;

import org.wso2.carbon.cep.sample.client.util.DataProvider;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class AnalyticsStatisticsClient {


    public static void main(String[] args)
            throws DataBridgeException, AgentException, MalformedURLException,
            AuthenticationException, TransportException, MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException {

        KeyStoreUtil.setTrustStoreParams();
        long totalEventCount = 5000000L;
        if (args.length >= 1) {
            totalEventCount = Long.valueOf(args[0]);
        }

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same

        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611", "admin", "admin");

        String streamId;
        try {
            streamId = dataPublisher.findStream("analytics_Statistics", "1.3.0");
        } catch (NoStreamDefinitionExistException e) {
            StreamDefinition streamDefinition = new StreamDefinition("analytics_Statistics", "1.3.0");
            streamDefinition.addMetaData("ipAdd", AttributeType.STRING);
            streamDefinition.addMetaData("index", AttributeType.LONG);
            streamDefinition.addMetaData("timestamp", AttributeType.LONG);
            streamDefinition.addPayloadData("userID", AttributeType.STRING);
            streamDefinition.addPayloadData("searchTerms", AttributeType.STRING);
            streamId = dataPublisher.defineStream(streamDefinition);

        }
        Thread.sleep(2000);

        System.out.println("Starting event sending...");
        long startTime = System.nanoTime();
        for (long i = 0; i < totalEventCount; i++) {
            Object[] metaDataArray = new Object[]{DataProvider.getMeta(), i, System.currentTimeMillis()};
            dataPublisher.publish(streamId, metaDataArray, null, DataProvider.getPayload());
            if ((i + 1) % 100000 == 0) {
                long elapsedTime = System.nanoTime() - startTime;
                double timeInSec = elapsedTime / 1000000000D;
                double throughputPerSec = (i + 1) / timeInSec;
                System.out.println("Sent " + (i + 1) + " events in " + timeInSec + " seconds with total throughput of " + throughputPerSec + " events per second.");
            }
        }

        Thread.sleep(1000);

        dataPublisher.stop();
    }
}
