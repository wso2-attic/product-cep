/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cep.sample.client;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ThriftReceiverServer {
    private static final ThriftReceiverServer testServer = new ThriftReceiverServer();
    private static AtomicLong totalEventCount = new AtomicLong(0);
    private static AtomicLong groupCount = new AtomicLong(0);
    private static long groupStartIndex = 0;
    private static int groupSize = 10000;
    private static long groupStartTimeNanos = 0;
    private static long groupStartTimeMillis = 0;
    private static long totalStartTimeNanos = 0;
    private static long totalStartTimeMillis = 0;
    private static boolean continuityCheckEnabled = false;
    private Logger log = org.apache.log4j.Logger.getLogger(ThriftReceiverServer.class);
    private ThriftDataReceiver thriftDataReceiver;

    public static void main(String[] args) throws DataBridgeException {
        testServer.start(7661);
        totalStartTimeNanos = groupStartTimeNanos = System.nanoTime();
        totalStartTimeMillis = groupStartTimeMillis = System.currentTimeMillis();
        synchronized (testServer) {
            try {
                testServer.wait();
            } catch (InterruptedException ignored) {
                System.out.println("Error: Thread interrupted; " + ignored.getMessage());
            }
        }
    }

    public void start(int receiverPort) throws DataBridgeException {
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

            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                measureThroughputUsingMillis(eventList);
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
        }
    }

    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
        System.out.println("Test Server Stopped");
    }

    private void measureThroughputUsingNanoTime(List<Event> eventList) {
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
            if (groupCount.compareAndSet(groupSize, 0)) {
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

    private void measureThroughputUsingMillis(List<Event> eventList) {
        String threadName = Thread.currentThread().getName();
        long receivedTime = System.currentTimeMillis();
        long producerIndex = 0;
        long timeStamp = 0;
        if (continuityCheckEnabled && !isEventsContinuous(eventList)) {
            log.warn("Non-continuous event in list. Dropped events??");
        }
        for (Event event : eventList) {
            long currentTotal = totalEventCount.incrementAndGet();
            long currentGroupCount = groupCount.incrementAndGet();
            producerIndex = (Long) event.getMetaData()[1];
            timeStamp = (Long) event.getMetaData()[2];
            log.trace("[" + threadName + "]: Received event with index |" + producerIndex + "|, produced at |" + timeStamp + "|, with payload : " + Arrays.toString(event.getPayloadData()));
            if (groupCount.compareAndSet(groupSize, 0)) {
                double elapsedTimeInSec = (receivedTime - groupStartTimeMillis) / 1000D;
                double totalElapsedTimeInSec = (receivedTime - totalStartTimeMillis) / 1000D;
                double totalThroughput = totalEventCount.get() / totalElapsedTimeInSec;
                double throughput = currentGroupCount / elapsedTimeInSec;
                log.info("[" + threadName + "] Elapsed time :|" + totalElapsedTimeInSec + "| - Received " + currentGroupCount + " events, from index |" + groupStartIndex
                        + "| to |" + producerIndex + "| with throughput of |" + throughput + "| events/second; Total events : |" + currentTotal
                        + "| with total throughput of |" + totalThroughput + "| events/second");
                groupStartTimeMillis = System.currentTimeMillis();
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
}