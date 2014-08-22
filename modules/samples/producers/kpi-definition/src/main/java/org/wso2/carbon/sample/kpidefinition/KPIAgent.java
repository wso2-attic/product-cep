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
package org.wso2.carbon.sample.kpidefinition;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
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

public class KPIAgent {
    private static Logger logger = Logger.getLogger(KPIAgent.class);
    public static final String PHONE_RETAIL_STREAM = "org.wso2.sample.phone.retail.store.kpi";
    public static final String VERSION = "1.0.0";

    public static final String[] phoneModels = {"Nokia", "Apple", "Samsung", "Sony-Ericson", "LG"};
    public static final String[] users = {"James", "Mary", "John", "Peter", "Harry", "Tom", "Paul"};
    public static final int[] quantity = {2, 5, 3, 4, 1};
    public static final int[] price = {50000, 55000, 90000, 80000, 70000};


    public static void main(String[] args) throws AgentException,
                                                  MalformedStreamDefinitionException,
                                                  StreamDefinitionException,
                                                  DifferentStreamDefinitionAlreadyDefinedException,
                                                  MalformedURLException,
                                                  AuthenticationException,
                                                  NoStreamDefinitionExistException,
                                                  TransportException, SocketException,
                                                  org.wso2.carbon.databridge.commons.exception.AuthenticationException {
        System.out.println("Starting Phone Retail Shop KPI Agent");

        KeyStoreUtil.setTrustStoreParams();

        String host = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];
        int events = Integer.parseInt(args[4]);


        //create data publisher

        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId = null;

        try {
            streamId = dataPublisher.findStream(PHONE_RETAIL_STREAM, VERSION);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            //Define event stream
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
                                                  "          {'name':'user','type':'STRING'}" +
                                                  "  ]" +
                                                  "}");
        }


        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);

            for (int i = 0; i < events; i++) {
                publishEvents(dataPublisher, streamId, i);
                System.out.println("Events published : " + (i + 1));
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int i) throws AgentException {
        int quantity = getRandomQuantity();
        Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                                   new Object[]{getRandomProduct(), quantity, quantity * getRandomPrice(), getRandomUser()});
        dataPublisher.publish(eventOne);
    }

    private static String getRandomProduct() {
        return phoneModels[getRandomId(5)];
    }

    private static String getRandomUser() {
        return users[getRandomId(7)];
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
