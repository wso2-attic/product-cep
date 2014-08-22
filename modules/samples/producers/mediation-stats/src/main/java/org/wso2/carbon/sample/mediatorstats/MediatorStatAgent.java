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
package org.wso2.carbon.sample.mediatorstats;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediatorStatAgent {

    private static Logger logger = Logger.getLogger(MediatorStatAgent.class);
    public static final String MEDIATOR_STATISTICS_STREAM = "org.wso2.sample.mediation.stats";
    public static final String VERSION = "1.0.0";

    private static final String[] DIRECTION = {"In", "Out"};
    private static final String[] STATUS_TYPE = {"Proxy", "Sequence", "Endpoint"};
    private static final String[] HOST = {"127.0.0.1", "127.0.0.2", "127.0.0.3", "127.0.0.4"};
    private static Long[] timestamps;

    static {

        long currentTimeStamp = System.currentTimeMillis();

        List<Long> timeStampList = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            timeStampList.add(currentTimeStamp - 1000 * (getRandomId(0, 3600)));
            timeStampList.add(currentTimeStamp - 3789 * getRandomId(3600, 86400));
            timeStampList.add(currentTimeStamp - 6789 * 86400 * getRandomId(1, 30));
            timeStampList.add(currentTimeStamp - 8789 * getRandomId(30 * 86400, 12 * 30 * 86400));
            timeStampList.add(currentTimeStamp - 9789 * getRandomId(12 * 30 * 86400, 5 * 12 * 30 * 86400));
        }

        timestamps = timeStampList.toArray(new Long[timeStampList.size()]);
    }

    public static void main(String[] args) throws AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException,
            AuthenticationException,
            NoStreamDefinitionExistException,
            TransportException, SocketException,
            org.wso2.carbon.databridge.commons.exception.AuthenticationException {

        System.out.println("Starting Mediator Statistic Sample");

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
            streamId = dataPublisher.findStream(MEDIATOR_STATISTICS_STREAM, VERSION);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                    "  'name':'" + MEDIATOR_STATISTICS_STREAM + "'," +
                    "  'version':'" + VERSION + "'," +
                    "  'nickName': 'MediationStatsDataAgent'," +
                    "  'description': 'A sample for Mediator Statistics'," +
                    "  'metaData':[" +
                    "          {'name':'host','type':'STRING'}" +
                    "  ]," +
                    "  'payloadData':[" +
                    "          {'name':'direction','type':'STRING'}," +
                    "          {'name':'timestamp','type':'LONG'}," +
                    "          {'name':'resource_id','type':'STRING'}," +
                    "          {'name':'stats_type','type':'STRING'}," +
                    "          {'name':'max_processing_time','type':'LONG'}," +
                    "          {'name':'avg_processing_time','type':'DOUBLE'}," +
                    "          {'name':'min_processing_time','type':'LONG'}," +
                    "          {'name':'fault_count','type':'INT'}," +
                    "          {'name':'count','type':'INT'}" +
                    "  ]" +
                    "}");
        }


        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);

            for (int i = 0; i < events; i++) {
                publishEvents(dataPublisher, streamId);
                System.out.println("Events published : " + (i + 1));
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
            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId) throws AgentException {
        Event eventOne = new Event(streamId, System.currentTimeMillis(), getMetadata(), null, getPayloadData());
        dataPublisher.publish(eventOne);
    }

    private static Object[] getMetadata() {
        return new Object[]{
                getRandomHost()
        };
    }

    private static Object[] getPayloadData() {
        Long maxProcessingTime = getMaxProcessingTime();
        Long minProcessingTime = getMinProcessingTime();
        Double avg = (maxProcessingTime.doubleValue() + minProcessingTime.doubleValue()) / 2;
        String type = getRandomType();
        return new Object[]{
                getRandomDirection(),
                timestamps[getRandomId(34)],
                "Simple_Stock_Quote_Service_" + type,
                type,
                maxProcessingTime,
                avg,
                minProcessingTime,
                getRandomId(2),
                1
        };
    }

    public static String getRandomHost() {
        return HOST[getRandomId(63) % 4];
    }

    private static String getRandomType() {
        return STATUS_TYPE[getRandomId(3)];
    }

    private static Long getMinProcessingTime() {
        return (long) getRandomId(400, 800);
    }

    private static Long getMaxProcessingTime() {
        return (long) getRandomId(1200, 1600);
    }

    private static String getRandomDirection() {
        return DIRECTION[getRandomId(2)];
    }

    private static int getRandomId(int i) {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
    }

    private static int getRandomId(int i, int j) {
        Random r = new Random();
        return r.nextInt(j - i + 1) + i;
    }

}
