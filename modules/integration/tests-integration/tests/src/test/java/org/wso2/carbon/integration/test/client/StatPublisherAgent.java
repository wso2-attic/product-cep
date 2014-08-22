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
package org.wso2.carbon.integration.test.client;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import java.net.MalformedURLException;
import java.util.*;

public class StatPublisherAgent {
    public static final String STREAM_NAME1 = "org.wso2.sample.service.data";
    public static final String VERSION1 = "1.0.0";
    public static String[] hosts = {"esb.foo.org", "dss.foo.org", "as.it.foo.org", "as.mkt.foo.com",
            "as.foo.org"};

    public static String[] remoteAddresses = {"94.167.250.236", "34.33.134.212", "237.120.89.29",
            "103.216.158.196", "81.225.246.119", "205.42.95.109",
            "100.103.41.21", "93.41.15.186", "207.10.167.241",
            "43.46.199.25"};

    public static long[] responseTimes = {19, 1, 2, 31, 4, 10, 3, 1, 144, 600};

    public static Long[] timestamps;

    public static Map<String, List<String>> services = new HashMap<String, List<String>>();


    static {
        long currentTimeStamp = System.currentTimeMillis();
        List<Long> timeStampList = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            timeStampList.add(currentTimeStamp - 1000 * (genRandNumber(3600, 0)));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(86400, 3600));
            timeStampList.add(currentTimeStamp - 1000 * 86400 * genRandNumber(30, 1));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400, 30 * 86400));
            timeStampList.add(currentTimeStamp - 1000 * genRandNumber(12 * 30 * 86400, 12 * 30 * 86400));
        }
        timestamps = timeStampList.toArray(new Long[timeStampList.size()]);
    }

    static {
        List<String> operations = new ArrayList<String>();
        operations.add("creditAmount");
        operations.add("checkCredit");

        services.put("creditService", operations);

        operations.clear();
        operations.add("shipOrder");

        services.put("shippingService", operations);

        operations.clear();
        operations.add("validateOrder");
        operations.add("validateShipping");

        services.put("validationService", operations);

        operations.clear();
        operations.add("checkoutOrder");

        services.put("checkoutService", operations);

        operations.clear();
        operations.add("getCustomerInfo");
        operations.add("getCustomers");
        operations.add("addCustomer");
        operations.add("editCustomerInfo");
        operations.add("removeCustomer");

        services.put("customerInfoService", operations);
    }

    private static int genRandNumber(int max, int min) {
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

    public static void start(int events) throws MalformedURLException, AgentException, AuthenticationException, TransportException, MalformedStreamDefinitionException, StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException {
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611" , "admin", "admin");

        String streamId1 = dataPublisher.defineStream("{" +
                "  'name':'" + STREAM_NAME1 + "'," +
                "  'version':'" + VERSION1 + "'," +
                "  'nickName': 'Statistics'," +
                "  'description': 'Service statistics'," +
                "  'metaData':[" +
                "          {'name':'request_url','type':'STRING'}," +
                "          {'name':'remote_address','type':'STRING'}," +
                "          {'name':'content_type','type':'STRING'}," +
                "          {'name':'user_agent','type':'STRING'}," +
                "          {'name':'host','type':'STRING'}," +
                "          {'name':'referer','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'service_name','type':'STRING'}," +
                "          {'name':'operation_name','type':'STRING'}," +
                "          {'name':'timestamp','type':'LONG'}," +
                "          {'name':'response_time','type':'LONG'}," +
                "          {'name':'request_count','type':'INT'}," +
                "          {'name':'response_count','type':'INT'}," +
                "          {'name':'fault_count','type':'INT'}" +
                "  ]" +
                "}");

        for (int i = 0; i < events; i++){
            publishEvents(dataPublisher, streamId1);
        }
        dataPublisher.stop();
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId) throws AgentException {

        Random rand = new Random();
        int hostIndex = rand.nextInt(5);
        String host = hosts[hostIndex];
        int serviceIndex = rand.nextInt(4);
        Iterator<String> serviceIterator = services.keySet().iterator();

        int k = 0;
        String service = null;
        while (serviceIterator.hasNext() && k < serviceIndex) {
            service = serviceIterator.next();
        }

        if (service == null) {
            service = serviceIterator.next();
        }

        List<String> operations = services.get(service);
        int operationIndex = rand.nextInt(operations.size());
        String operation = operations.get(operationIndex);

        Object[] meta = new Object[]{
            "http://" + host + "/services/" + service,
            remoteAddresses[rand.nextInt(10)],
            "application/xml",
            "http-components/client",
            host,
            "http://example.org"
            };

        int response = rand.nextInt(2);

        Object[] payload = new Object[]{
            service,
            operation,
            timestamps[rand.nextInt(34)], // Unix timeStamp
            responseTimes[rand.nextInt(10)],
            1,
            response,
            (response == 0) ? 1 : 0, // fault flag
        };


        Event statisticsEvent = new Event(streamId, System.currentTimeMillis(), meta, null, payload);
        dataPublisher.publish(statisticsEvent);

    }
}
