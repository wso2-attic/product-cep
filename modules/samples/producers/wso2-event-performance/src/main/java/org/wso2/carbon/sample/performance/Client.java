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

package org.wso2.carbon.sample.performance;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Client {
    private static Logger log = Logger.getLogger(Client.class);

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

            //create data publisher
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username,
                    password);

            //Publish event for a valid stream
            publishEvents(dataPublisher, Integer.parseInt(eventCount), Integer.parseInt(elapsedCount));

            dataPublisher.shutdownWithAgent();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, int eventCount, int elapsedCount) {
        int counter = 0;
        Random randomGenerator = new Random();
        String streamId = "org.wso2.event.sensor.stream:1.0.0";
        long lastTime = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat("#");

        while (counter < eventCount) {
            Event event = new Event(streamId, System.currentTimeMillis(),
                    new Object[]{System.currentTimeMillis(), randomGenerator.nextBoolean(), counter,
                            "temperature-" + counter},
                    new Object[]{randomGenerator.nextDouble(), randomGenerator.nextDouble()},
                    new Object[]{randomGenerator.nextFloat(), randomGenerator.nextDouble()});

            dataPublisher.publish(event);

            if ((counter + 1) % elapsedCount == 0) {

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastTime;
                double throughputPerSecond = (((double)elapsedCount) / elapsedTime) * 1000;
                lastTime = currentTime;
                log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime
                        + " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) + " events per second.");
            }

            counter++;
        }
    }
}
