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
package org.wso2.carbon.cep.sample.client;

import org.apache.log4j.Logger;
import org.wso2.carbon.cep.sample.client.util.DataProvider;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class AnalyticsStatisticsClient {


    private static Logger log = Logger.getLogger(AnalyticsStatisticsClient.class);

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
            streamDefinition.addMetaData("nanoTime", AttributeType.LONG);
            streamDefinition.addPayloadData("userID", AttributeType.STRING);
            streamDefinition.addPayloadData("searchTerms", AttributeType.STRING);
            streamId = dataPublisher.defineStream(streamDefinition);

        }
        Thread.sleep(2000);

        System.out.println("Starting event sending...");
        long startTime = System.nanoTime();
        for (long i = 0; i < totalEventCount; i++) {
            Object[] metaDataArray = new Object[]{DataProvider.getMeta(), i, System.currentTimeMillis(), System.nanoTime()};
            dataPublisher.publish(streamId, metaDataArray, null, DataProvider.getPayload());
            if ((i + 1) % 100000 == 0) {
                long elapsedTime = System.nanoTime() - startTime;
                double timeInSec = elapsedTime / 1000000000D;
                double throughputPerSec = (i + 1) / timeInSec;
                log.info("Sent " + (i + 1) + " events in " + timeInSec + " seconds with total throughput of " + throughputPerSec + " events per second.");
                startTime = System.nanoTime();
            }
        }

        Thread.sleep(1000);

        dataPublisher.stop();
    }
}
