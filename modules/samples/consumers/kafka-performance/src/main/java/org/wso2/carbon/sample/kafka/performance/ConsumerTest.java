/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.kafka.performance;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsumerTest implements Runnable {
    private static Logger log = Logger.getLogger(ConsumerTest.class);
    DecimalFormat decimalFormat = new DecimalFormat("#");

    private KafkaStream kafkaStream;
    private static long lastTime;
    private static int elapsedCount = 1000000;
    private long sentTime = 0;
    private long receivedTime = 0;

    //Atomic Variables
    private AtomicLong eventCount;
    private static AtomicLong latency;

    public ConsumerTest(KafkaStream stream) {
        kafkaStream = stream;
        eventCount = new AtomicLong(0);
        latency = new AtomicLong(0);
        lastTime = System.currentTimeMillis();
    }

    public void run() {
        try {
            ConsumerIterator<byte[], byte[]> iterator = kafkaStream.iterator();

            while (iterator.hasNext()) {
                String message = new String(iterator.next().message());
                receivedTime = System.currentTimeMillis();

                //Pattern match for time stamp
                Pattern eventPattern = Pattern.compile("(<timestamp>(\\d+))");
                Matcher eventPatternMatcher = eventPattern.matcher(message);
                if (eventPatternMatcher.find()) {
                    sentTime = Long.parseLong(eventPatternMatcher.group(2));
                } else {
                    log.error("unable to extract timestamp from received event");
                }

                latency.addAndGet(receivedTime - sentTime);
                eventCount.addAndGet(1);

                if (eventCount.get() % elapsedCount == 0) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastTime;
                    double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                    lastTime = currentTime;
                    log.info("Received " + elapsedCount + " sensor events in " + elapsedTime +
                            " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                            " events per second. Average latency is " + (double) latency.get() / elapsedCount + " milliseconds per event.");
                }
            }
        } catch (Throwable t) {
            log.error("Error when receiving messages", t);
        }
    }
}


