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
package org.wso2.carbon.sample.stockquote;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockQuoteAgent {

    private static long sentEventCount = 0;
    public static final String STREAM_NAME1 = "stock_quote";
    public static final String VERSION1 = "1.3.0";

    private static String host = "localhost";
    private static String port = "7611";
    private static String username = "admin";
    private static String password = "admin";
    private static int events = 1000;

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


    public static void main(String[] args)
            throws AgentException, MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException, NoStreamDefinitionExistException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException,
            TransportException, SocketException {
        System.out.println("Starting Statistics Agent");
        System.out.println("Starting Statistics Agent");
        KeyStoreUtil.setTrustStoreParams();

        host = args[0];
        port = args[1];
        username = args[2];
        password = args[3];
        events = Integer.parseInt(args[4]);


        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);


        StreamDefinition streamDefinition = new StreamDefinition(STREAM_NAME1, VERSION1);
        streamDefinition.addPayloadData("price", AttributeType.INT);
        streamDefinition.addPayloadData("symbol", AttributeType.STRING);
        String streamId = dataPublisher.defineStream(streamDefinition);


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

            dataPublisher.stop();
        }
    }


    private static Object[] getPayload() {
        int symbolIndex = Math.round((float) Math.random() * 3);
        int priceIndex = Math.round((float) Math.random() * 4);
        return new Object[]{PRICES.get(priceIndex), SYMBOL.get(symbolIndex)};
    }
}

