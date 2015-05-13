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

public class StockQuoteClient {
    private static long sentEventCount = 0;
    public static final String STREAM_NAME1 = "stock_quote";
    public static final String VERSION1 = "1.3.0";

    private static final List<String> SYMBOL = Collections.unmodifiableList(new ArrayList<String>() {{
        add("IBM");
        add("WSO2");
        add("MSFT");
        add("ORACLE");
    }});

    private static final List<Integer> PRICES = Collections.unmodifiableList(new ArrayList<Integer>() {{
        add(50);
        add(75);
        add(100);
        add(125);
        add(150);
    }});


    public static void publish(String host, String port, String username, String password, int events)
            throws MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException, NoStreamDefinitionExistException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException,
            TransportException, SocketException, DataEndpointAgentConfigurationException, DataEndpointException, DataEndpointAuthenticationException, DataEndpointConfigurationException {
        System.out.println("Starting Stock quote Agent");

        KeyStoreUtil.setTrustStoreParams();

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME1, VERSION1);

        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);

            while (sentEventCount < events) {
                dataPublisher.publish(streamId, null, null, getPayload());
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
        int symbolIndex = Math.round((float) Math.random() * 3);
        int priceIndex = Math.round((float) Math.random() * 4);
        return new Object[]{PRICES.get(priceIndex), SYMBOL.get(symbolIndex)};
    }
}
