/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Client {
    private static Log log = LogFactory.getLog(Client.class);

    public static void main(String[] args) {
        log.info(Arrays.deepToString(args));
        try {
            log.info("Starting WSO2 Performance Test Client");

            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath());
            DataPublisherUtil.setTrustStoreParams();

            String protocol = args[0];
            String host = args[1];
            String port = args[2];
            String username = args[3];
            String password = args[4];
            String eventCount = args[5];
            String elapsedCount = args[6];
            String warmUpCount = args[7];
            String calcType = args[8];

            //create data publisher
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username,
                    password);

            //Publish event for a valid stream
            if ("latency".equalsIgnoreCase(calcType)) {
                publishEventsForLatency(dataPublisher, Long.parseLong(eventCount), Long.parseLong(elapsedCount),
                        Long.parseLong(warmUpCount));
            } else {
                publishEvents(dataPublisher, Long.parseLong(eventCount), Long.parseLong(elapsedCount),
                        Long.parseLong(warmUpCount));
            }

            dataPublisher.shutdownWithAgent();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, long eventCount, long elapsedCount,
                                      long warmUpCount) {
        long counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";
        long lastTime = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat("#");

        while (counter < eventCount) {
            boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
            int sensorId = randomGenerator.nextInt();
            double longitude = randomGenerator.nextDouble();
            double latitude = randomGenerator.nextDouble();
            float humidity = randomGenerator.nextFloat();
            double sensorValue = randomGenerator.nextDouble();
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, sensorId,
                            "temperature-" + counter},
                    new Object[]{longitude, latitude},
                    new Object[]{humidity, sensorValue});

            dataPublisher.publish(event);

            if ((counter > warmUpCount) && ((counter + 1) % elapsedCount == 0)) {

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastTime;
                double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                lastTime = currentTime;
                log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime
                        + " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond)
                        + " events per second.");
            }

            counter++;
        }
    }

    private static void sendWarmUpEvents(DataPublisher dataPublisher, long warmUpCount) {
        long counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";

        while (counter < warmUpCount) {
            boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
            int sensorId = randomGenerator.nextInt();
            double longitude = randomGenerator.nextDouble();
            double latitude = randomGenerator.nextDouble();
            float humidity = randomGenerator.nextFloat();
            double sensorValue = randomGenerator.nextDouble();
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, sensorId,
                            "warmup-" + counter},
                    new Object[]{longitude, latitude},
                    new Object[]{humidity, sensorValue});

            dataPublisher.publish(event);
            counter++;
        }
    }

    private static void publishEventsForLatency(DataPublisher dataPublisher, long eventCount, long elapsedCount,
                                                long warmUpCount) {
        sendWarmUpEvents(dataPublisher, warmUpCount);
        long counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";

        while (counter < eventCount) {
            boolean isPowerSaveEnabled = randomGenerator.nextBoolean();
            int sensorId = randomGenerator.nextInt();
            double longitude = randomGenerator.nextDouble();
            double latitude = randomGenerator.nextDouble();
            float humidity = randomGenerator.nextFloat();
            double sensorValue = randomGenerator.nextDouble();
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), isPowerSaveEnabled, sensorId,
                            "temperature-" + counter},
                    new Object[]{longitude, latitude},
                    new Object[]{humidity, sensorValue});

            dataPublisher.publish(event);
            log.info("Sent event " + counter + " at " + System.currentTimeMillis());

            if (elapsedCount > 0) {
                try {
                    Thread.sleep(elapsedCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            counter++;
        }
    }

}
