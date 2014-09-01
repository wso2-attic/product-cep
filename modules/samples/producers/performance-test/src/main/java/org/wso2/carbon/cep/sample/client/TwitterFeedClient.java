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

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class TwitterFeedClient {

    private static final int MAX_ITERATIONS = 1;

    public static void main(String[] args)
            throws DataBridgeException, AgentException, MalformedURLException,
            AuthenticationException, TransportException, MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            InterruptedException {

        KeyStoreUtil.setTrustStoreParams();
        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same

        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611", "admin", "admin");

        String streamId;
        try {
            streamId = dataPublisher.findStream("twitterFeed", "1.0.0");
        } catch (NoStreamDefinitionExistException e) {
            StreamDefinition streamDefinition = new StreamDefinition("twitterFeed", "1.0.0");
            streamDefinition.addPayloadData("company", AttributeType.STRING);
            streamDefinition.addPayloadData("wordCount", AttributeType.INT);
            streamId = dataPublisher.defineStream(streamDefinition);
        }
        Thread.sleep(1000);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            //In this case meta data, correlation data is null
            dataPublisher.publish(streamId, null, null, new Object[]{"MSFT", 8});
            System.out.println("TwitterFeed Message " + (i + 1) + " sent");
            Thread.sleep(3000);

            dataPublisher.publish(streamId, null, null, new Object[]{"MSFT", 6});
            System.out.println("TwitterFeed Message " + (i + 2) + " sent");
            Thread.sleep(2000);

            dataPublisher.publish(streamId, null, null, new Object[]{"WSO2", 16});
            System.out.println("TwitterFeed Message " + (i + 3) + " sent");
            Thread.sleep(2000);

            dataPublisher.publish(streamId, null, null, new Object[]{"MSFT", 12});
            System.out.println("TwitterFeed Message " + (i + 4) + " sent");
            Thread.sleep(2000);

            dataPublisher.publish(streamId, null, null, new Object[]{"WSO2", 6});
            System.out.println("TwitterFeed Message " + (i + 5) + " sent");
            Thread.sleep(2000);

            dataPublisher.publish(streamId, null, null, new Object[]{"WSO2", 2});
            System.out.println("TwitterFeed Message " + (i + 6) + " sent");
            Thread.sleep(3000);
        }

        dataPublisher.stop();
    }
}
