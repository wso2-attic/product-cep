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
package org.wso2.sample.logininfo.client;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.*;

public class SuspiciousLoginsDetector {

    private static int sentEventCount = 0;
    public static final String STREAM_NAME1 = "org.wso2.sample.login.data";
    public static final String VERSION1 = "1.1.0";

    public static String[] userNames = {"paul", "mike", "roberto", "stuart", "steve", "mike", "adam", "cameron",
    "graham", "stuart"};

    public static String[] ipAddresses = {"94.167.250.236", "34.33.134.212", "237.120.89.29",
            "103.216.158.196", "81.225.246.119", "205.42.95.109",
            "100.103.41.21", "93.41.15.186", "207.10.167.241",
            "43.46.199.25"};
    public static String[] browsers = {"Firefox", "IE9", "Safari", "Chrome"};


    public static void main(String[] args)
            throws AgentException, MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException, NoStreamDefinitionExistException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException,
            TransportException, SocketException {
        System.out.println("Starting Login info Agent");

        KeyStoreUtil.setTrustStoreParams();

        String host = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];
        int events = Integer.parseInt(args[4]);

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId1 = null;

        try {
            streamId1 = dataPublisher.findStream(STREAM_NAME1, VERSION1);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            System.out.println("Stream doesn't exist. Creating a new stream.");

            streamId1 = dataPublisher.defineStream("{" +
                    "  'name':'" + STREAM_NAME1 + "'," +
                    "  'version':'" + VERSION1 + "'," +
                    "  'nickName': 'Statistics'," +
                    "  'description': 'login analyzer'," +
                    "  'payloadData':[" +
                    "          {'name':'user_name','type':'STRING'}," +
                    "          {'name':'ip_address','type':'STRING'}," +
                    "          {'name':'browser','type':'STRING'}" +
                    "  ]" +
                    "}");
        }

        //Publish event for a valid stream
        if (!streamId1.isEmpty()) {
            System.out.println("Stream ID: " + streamId1);

            while (sentEventCount < events) {
                publishEvents(dataPublisher, streamId1, 1, sentEventCount);
                System.out.println("Events published : " + sentEventCount);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //ignore
            }
            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int eventLimit, int index)
            throws AgentException {

        Random rand = new Random();

        for (int i = 0; i < 3; i++) {
            int userIndex = index % userNames.length;
            String userName = userNames[userIndex];

            int ipIndex = rand.nextInt(ipAddresses.length);
            String ip = ipAddresses[ipIndex];

            int browserIndex = rand.nextInt(browsers.length);
            String browser = browsers[browserIndex];

            for (int j = 0; j < 3; j++) {

                Object[] meta = null;

                Object[] payload = new Object[]{
                        userName,
                        ip,
                        browser
                };

                Object[] correlation = null;

                Event statisticsEvent = new Event(streamId, System.currentTimeMillis(),
                        meta, correlation, payload);
                dataPublisher.publish(statisticsEvent);
                System.out.println("Event published: " + Arrays.toString(payload));
                if(++sentEventCount >= eventLimit) {
                    return;
                }
            }
        }
    }

}

