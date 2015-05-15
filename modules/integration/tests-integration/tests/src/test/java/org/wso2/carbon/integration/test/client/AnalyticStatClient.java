/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.integration.test.client;

import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalyticStatClient {
    private static long sentEventCount = 0;
    public static final String STREAM_NAME1 = "analytics_Statistics";
    public static final String VERSION1 = "1.3.0";

    private static final int MULTIPLIER = 14;

    private static final List<String> SEARCH_TERMS = Collections.unmodifiableList(new ArrayList<String>() {{
        add("ORACLE");
        add("WSO2");
        add("Complex Event Processing");
        add("ESB");
        add("MSFT");
        add("IBM");
        add("jaggery");
        add(".jag files");
        add("Siddhi");
        add("siddhi query language");
        add("CEP performance numbers");
        add("Complex Eventing");
        add("CEP throughput");
        add("WSO2Con");
        add("WSO2 Roadmap");
    }});

    private static final List<String> IP_ADDRESSES = Collections.unmodifiableList(new ArrayList<String>() {{
        add("192.168.1.100");
        add("192.168.1.23");
        add("192.168.1.240");
        add("192.168.0.2");
        add("10.8.0.1");
        add("10.0.1.75");
        add("10.8.8.23");
        add("10.8.1.224");
        add("203.94.106.11");
        add("203.94.10.15");
        add("203.90.106.110");
        add("203.93.106.23");
        add("192.248.8.68");
        add("116.24.5.63");
        add("124.14.5.135");
    }});

    private static final List<String> USER_IDS = Collections.unmodifiableList(new ArrayList<String>() {{
        add("ann@org1.com");
        add("sam@org2.com");
        add("jeff@wso2.com");
        add("gayan@org5.com");
        add("lang@wso2.org");
        add("manjula@org5.org");
        add("john@org3.com");
        add("chris@org1.com");
        add("dennis@org7.com");
        add("mary@org2.com");
        add("gavin@wso2.org");
        add("oleg@org6.com");
        add("marvin@org4.com");
        add("naveen@wso2.com");
        add("praveen@org2.org");
    }});


    public static void publish(String host, String port, String username, String password, int events)
            throws  MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException, NoStreamDefinitionExistException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException,
            TransportException, SocketException, DataEndpointAgentConfigurationException, DataEndpointException,
            DataEndpointAuthenticationException, DataEndpointConfigurationException {
        System.out.println("Starting Statistics Agent");
        KeyStoreUtil.setTrustStoreParams();

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);


//        StreamDefinition streamDefinition = new StreamDefinition(STREAM_NAME1, VERSION1);
//        streamDefinition.addMetaData("ipAdd", AttributeType.STRING);
//        streamDefinition.addMetaData("index", AttributeType.LONG);
//        streamDefinition.addMetaData("timestamp", AttributeType.LONG);
//        streamDefinition.addMetaData("nanoTime", AttributeType.LONG);
//        streamDefinition.addPayloadData("userID", AttributeType.STRING);
//        streamDefinition.addPayloadData("searchTerms", AttributeType.STRING);
//        String streamId = dataPublisher.defineStream(streamDefinition);

        String streamId = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME1, VERSION1);
        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);

            while (sentEventCount < events) {
                dataPublisher.publish(streamId, getMeta(), null, getPayload());
                sentEventCount++;
                System.out.println("Events published : " + sentEventCount);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //ignore
            }

            dataPublisher.shutdown();
        }
    }


    private static Object[] getPayload() {
        int userIndex = Math.round((float) Math.random() * MULTIPLIER);
        int termIndex = Math.round((float) Math.random() * MULTIPLIER);
        return new Object[]{USER_IDS.get(userIndex), SEARCH_TERMS.get(termIndex)};
    }

    private static Object[] getMeta() {
        int ipIndex = Math.round((float) Math.random() * MULTIPLIER);
        return new Object[]{IP_ADDRESSES.get(ipIndex), sentEventCount, System.currentTimeMillis(), System.nanoTime()};
    }
}
