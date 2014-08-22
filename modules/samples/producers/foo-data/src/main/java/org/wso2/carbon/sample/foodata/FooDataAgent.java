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
package org.wso2.carbon.sample.foodata;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Random;

public class FooDataAgent {

    private static Logger logger = Logger.getLogger(FooDataAgent.class);
    public static final String FOO_DATA_STREAM = "org.foo.data";
    public static final String VERSION = "1.0.0";

    private static final String DATA = "Data";
    private static final String TYPE = "TYPE";
    private static final String[] HOST = {"127.0.0.1", "127.0.0.2", "127.0.0.3", "127.0.0.4"};

    public static void main(String[] args) throws AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException,
            NoStreamDefinitionExistException,
            TransportException, SocketException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException {

        System.out.println("Starting Foo Data Sample");

        KeyStoreUtil.setTrustStoreParams();

        String mode = args[0];
        String host1 = args[1];
        String port1 = args[2];
        String host2 = args[3];
        String port2 = args[4];
        String username = args[5];
        String password = args[6];
        int events = Integer.parseInt(args[7]);

        //create data publisher

        DataPublisher dataPublisher1 = createDataPublisher(host1, port1, username, password);
        DataPublisher dataPublisher2 = createDataPublisher(host2, port2, username, password);


        //Publish event for a valid stream

        for (int i = 0; i < events; i++) {
            Event event = createEvent();
            if (mode.equalsIgnoreCase("replicate")) {
                dataPublisher1.publish(event);
                System.out.println("Events published :" + (i + 1) + " to host:" + host1 + " port:" + port1);

                dataPublisher2.publish(event);
                System.out.println("Events published :" + (i + 1) + " to host:" + host2 + " port:" + port2);

            } else if(mode.equalsIgnoreCase("loadbalance")){
                if (i % 2 == 0) {
                    dataPublisher1.publish(event);
                    System.out.println("Events published :" + (i + 1) + " to host:" + host1 + " port:" + port1);
                } else {
                    dataPublisher2.publish(event);
                    System.out.println("Events published :" + (i + 1) + " to host:" + host2 + " port:" + port2);

                }
            } else {
                System.out.println("mode :"+mode+" not found, it need to be either replicate or loadbalance ");
            }
            if ((i % 100) == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Do nothing. Just add time buffer
                }
            }
        }
        try {

            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        dataPublisher1.stop();
        dataPublisher2.stop();
    }


    private static DataPublisher createDataPublisher(String host1, String port1, String username, String password) throws MalformedURLException, AgentException, org.wso2.carbon.databridge.commons.exception.AuthenticationException, TransportException, MalformedStreamDefinitionException, StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException {
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host1 + ":" + port1, username, password);
        String streamId = dataPublisher.defineStream("{" +
                "  'name':'" + FOO_DATA_STREAM + "'," +
                "  'version':'" + VERSION + "'," +
                "  'metaData':[" +
                "          {'name':'host','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'data','type':'STRING'}," +
                "          {'name':'type','type':'STRING'}," +
                "          {'name':'timestamp','type':'LONG'}" +
                "  ]" +
                "}");

        System.out.println("Stream ID: " + streamId);
        return dataPublisher;
    }

    private static Event createEvent() throws AgentException {
        return new Event(FOO_DATA_STREAM + ":" + VERSION, System.currentTimeMillis(), getMetadata(), null, getPayloadData());
    }

    private static Object[] getMetadata() {
        return new Object[]{
                getRandomHost()
        };
    }

    private static Object[] getPayloadData() {
        return new Object[]{
                getRandomData(),
                getRandomType(),
                System.currentTimeMillis()
        };
    }

    public static String getRandomHost() {
        return HOST[getRandomId(63) % 4];
    }

    private static String getRandomType() {
        return TYPE + getRandomId(10);
    }

    private static String getRandomData() {
        return DATA + getRandomId(20);
    }

    private static int getRandomId(int i) {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
    }

}
