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
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PerfTestClient {
    private final static PerfTestClient perfTestClient = new PerfTestClient();
    private final static Object warmUpLock = new Object();
    public static final int GROUP_SIZE = 100000;
    private static int receiverGroupSize = 10000;
    private static long totalEvents = 1010000L;
    private static int warmUpThreshold = 100000;
    private static long sleepAfterWarmUpMillis = 15000;
    private static AtomicLong totalEventCount = new AtomicLong(0);
    private static AtomicLong groupCount = new AtomicLong(0);
    private static long groupStartIndex = 0;
    private static long groupStartTimeNanos = 0;
    private static long totalStartTimeNanos = 0;
    private static boolean longRunningMode = false;
    private static boolean continuityCheckEnabled = false;
    private static boolean receiverOnSameJvm = false;
    private Logger log = org.apache.log4j.Logger.getLogger(PerfTestClient.class);
    private ThriftDataReceiver thriftDataReceiver;

    public static void main(String[] args) throws DataBridgeException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar_name> <totalEvents> <receiverGroupSize> <no of events as threshold> <sleep after warm up millis>");
            System.out.println("Eg: java -jar testClientRcvrProducer.jar 1010000 10000 100000 15000");
            System.out.println("Specifying arguments is optional.");
        }
        if (args.length > 0) {
            totalEvents = Long.valueOf(args[0]);
            if (totalEvents <= 0) {
                longRunningMode = true;
            }
        }
        if (args.length > 1) {
            receiverGroupSize = Integer.valueOf(args[1]);
        }
        if (args.length > 2) {
            warmUpThreshold = Integer.valueOf(args[2]);
        }
        if (args.length > 3) {
            sleepAfterWarmUpMillis = Long.valueOf(args[3]);
        }

        ThriftReceiver thriftReceiver = null;
        Thread testServerThread = null;
        if (receiverOnSameJvm) {
            thriftReceiver = perfTestClient.new ThriftReceiver();
            thriftReceiver.setReceiverPort(7661);
            testServerThread = new Thread(thriftReceiver);
        }
        ThriftPublisher thriftPublisher = perfTestClient.new ThriftPublisher();
        Thread publisherClient = new Thread(thriftPublisher);
        synchronized (perfTestClient) {
            try {
                if (testServerThread != null) {
                    testServerThread.start();
                    Thread.sleep(2000);
                }
                publisherClient.start();
                perfTestClient.wait();
                if (thriftReceiver != null) {
                    thriftReceiver.stop();
                }
            } catch (InterruptedException e) {
                System.out.println("Error: Thread interrupted; " + e.getMessage());
            }
        }
    }

    private class ThriftPublisher implements Runnable {

        @Override
        public void run() {
            try {
                startThriftPublisher();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void startThriftPublisher() throws InterruptedException, AgentException, MalformedStreamDefinitionException, StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, TransportException, AuthenticationException, MalformedURLException {
            KeyStoreUtil.setTrustStoreParams();
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
            if (longRunningMode) {
                System.out.println("Entering long running mode...");
                long startTime = System.nanoTime();
                boolean burstMode = false;
                for (long i = 0; i < Long.MAX_VALUE; i++) {
                    Object[] metaDataArray = new Object[]{DataProvider.getMeta(), i, System.currentTimeMillis(), System.nanoTime()};
                    dataPublisher.publish(streamId, metaDataArray, null, DataProvider.getPayload());
                    if (!burstMode) {
                        int sleepTime = (int) Math.round(Math.random() * 100);
                        Thread.sleep(sleepTime);
                    }
                    if ((i + 1) % GROUP_SIZE == 0) {
                        long elapsedTime = System.nanoTime() - startTime;
                        double timeInSec = elapsedTime / 1000000000D;
                        double throughputPerSec = GROUP_SIZE / timeInSec;
                        System.out.println("Total events: " + (i + 1) + ", sent " + GROUP_SIZE + " events in " + timeInSec + " seconds with throughput of " + throughputPerSec + " events per second.");
                        burstMode = !burstMode;
                        startTime = System.nanoTime();
                    }
                }
            } else {
                System.out.println("Entering performance test mode...");
                long startTime = System.nanoTime();
                for (long i = 0; i < totalEvents; i++) {
                    Object[] metaDataArray = new Object[]{DataProvider.getMeta(), i, System.currentTimeMillis(), System.nanoTime()};
                    dataPublisher.publish(streamId, metaDataArray, null, DataProvider.getPayload());
                    if (i % GROUP_SIZE == (GROUP_SIZE - 1)) {
                        if (i == (warmUpThreshold - 1)) {
                            Thread.sleep(sleepAfterWarmUpMillis);
                        }
                        long elapsedTime = System.nanoTime() - startTime;
                        double timeInSec = elapsedTime / 1000000000D;
                        double throughputPerSec = GROUP_SIZE / timeInSec;
                        System.out.println("Total events: " + (i + 1) + ", sent " + GROUP_SIZE + " events in " + timeInSec + " seconds with throughput of " + throughputPerSec + " events per second.");
                        startTime = System.nanoTime();
                    }
                }
            }

            Thread.sleep(1000);

            dataPublisher.stop();
        }
    }

    private class ThriftReceiver implements Runnable {
        private int receiverPort;
        private boolean warmedUp;

        @Override
        public void run() {
            KeyStoreUtil.setKeyStoreParams();
            DataBridge databridge = new DataBridge(new AuthenticationHandler() {
                @Override
                public boolean authenticate(String userName,
                                            String password) {
                    return true;// allays authenticate to true
                }

                @Override
                public String getTenantDomain(String userName) {
                    return "admin";
                }

                @Override
                public int getTenantId(String s) throws UserStoreException {
                    return -1234;
                }

                @Override
                public void initContext(AgentSession agentSession) {

                }

                @Override
                public void destroyContext(AgentSession agentSession) {

                }
            }, new InMemoryStreamDefinitionStore());
            thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

            databridge.subscribe(new AgentCallback() {
                @Override
                public void definedStream(StreamDefinition streamDefinition, int i) {
                    log.info("StreamDefinition " + streamDefinition);
                }

                @Override
                public void removeStream(StreamDefinition streamDefinition, int i) {
                    synchronized (perfTestClient) {
                        perfTestClient.notify();
                    }
                }

                @Override
                public void receive(List<Event> eventList, Credentials credentials) {
                    if (!warmedUp) {
                        long producerIndex = (Long) eventList.get(0).getMetaData()[1];
                        if (totalEventCount.get() <= warmUpThreshold) {
                            log.info("Received event list with starting index: " + producerIndex);
                            totalEventCount.addAndGet(eventList.size());
                        } else {
                            synchronized (warmUpLock) {
                                if (!warmedUp) {
                                    System.out.println("[" + Thread.currentThread().getName() + "] : Finished warm up with " + warmUpThreshold + " events. Starting with index :" + producerIndex);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        log.error("Sleep interrupted! : " + e.getMessage(), e);
                                    }
                                    totalStartTimeNanos = groupStartTimeNanos = System.nanoTime();
                                    totalEventCount.set(0);
                                    groupStartIndex = producerIndex;
                                    warmedUp = true;
                                }
                            }
                        }
                        return;
                    }
                    measureThroughput(eventList);
                }
            });


            try {
                String address = HostAddressFinder.findAddress("localhost");
                System.out.println("Test Server starting on " + address);
                log.info("Test Server starting on " + address);
                thriftDataReceiver.start(address);
                log.info("Test Server Started");
            } catch (SocketException e) {
                log.info("Test Server not started !" + e);
            } catch (DataBridgeException e) {
                log.info("Data bridge exception :" + e);
            }

        }

        private void measureThroughput(List<Event> eventList) {
            String threadName = Thread.currentThread().getName();
            long receivedTimeNanos = System.nanoTime();
            long producerIndex = 0;
            long timeStampNanos = 0;
            if (continuityCheckEnabled && !isEventsContinuous(eventList)) {
                log.warn("Non-continuous event in list. Dropped events??");
            }
            for (Event event : eventList) {
                long currentTotal = totalEventCount.incrementAndGet();
                long currentGroupCount = groupCount.incrementAndGet();
                producerIndex = (Long) event.getMetaData()[1];
                timeStampNanos = (Long) event.getMetaData()[3];
                log.trace("[" + threadName + "]: Received event with index |" + producerIndex + "|, produced at |" + timeStampNanos + "|, with payload : " + Arrays.toString(event.getPayloadData()));
                if (groupCount.compareAndSet(receiverGroupSize, 0)) {
                    double elapsedTimeInSec = (receivedTimeNanos - groupStartTimeNanos) / 1000000000D;
                    double totalElapsedTimeInSec = (receivedTimeNanos - totalStartTimeNanos) / 1000000000D;
                    double totalThroughput = totalEventCount.get() / totalElapsedTimeInSec;
                    double throughput = currentGroupCount / elapsedTimeInSec;
                    log.info("[" + threadName + "] Elapsed time :|" + totalElapsedTimeInSec + "| - Received " + currentGroupCount + " events, from index |" + groupStartIndex
                            + "| to |" + producerIndex + "| with throughput of |" + throughput + "| events/second; Total events : |" + currentTotal
                            + "| with total throughput of |" + totalThroughput + "| events/second");
                    groupStartTimeNanos = System.nanoTime();
                    groupStartIndex = producerIndex;
                }
            }
        }

        private boolean isEventsContinuous(List<Event> eventList) {
            long eventIndex = (Long) eventList.get(0).getMetaData()[1];
            for (Event event : eventList) {
                long producerIndex = (Long) event.getMetaData()[1];
                if (producerIndex != eventIndex++) {
                    return false;
                }
            }
            return true;
        }

        private void measureLatency(List<Event> eventList) {
            long receivedTimeNanos = System.nanoTime();
            long producerIndex = (Long) eventList.get(0).getMetaData()[1];
            long rcvCount = producerIndex;
            int eventCount = 0;
            long totalLatency = 0L;
            for (Event event : eventList) {
                producerIndex = (Long) event.getMetaData()[1];
                long startOfEventNanos = (Long) event.getMetaData()[3];
                if (producerIndex == rcvCount) {
                    long latency = (receivedTimeNanos - startOfEventNanos);
                    totalLatency += latency;
                    eventCount++;
                } else {
                    log.warn("[" + Thread.currentThread().getName() + "]: Event not received in order. Dropped event??");
                }
                rcvCount++;
            }
            double avgLatency = (totalLatency / eventCount) / 1000000000D;
            log.info("[" + Thread.currentThread().getName() + "]: A batch of " + eventCount
                    + " events received from index " + eventList.get(0).getMetaData()[1] + " to " + producerIndex + " with average latency " + avgLatency + " seconds.");
        }

        public void setReceiverPort(int receiverPort) {
            this.receiverPort = receiverPort;
        }

        public void stop() {
            thriftDataReceiver.stop();
            log.info("Test Server Stopped");
            System.out.println("Test Server Stopped");
        }

    }
}
