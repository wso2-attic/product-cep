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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.kafka.performance;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsumerTest implements Runnable {
    private static Log log = LogFactory.getLog(ConsumerTest.class);
    DecimalFormat decimalFormat = new DecimalFormat("#");

    private KafkaStream kafkaStream;

    private long sentTime = 0;
    private long receivedTime = 0;
    private static int elapsedCount = 100000;

    //Atomic Variables
    private static AtomicLong eventCount = new AtomicLong(0);
    private static AtomicLong latency = new AtomicLong(0);
    private static AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());

    public ConsumerTest(KafkaStream stream) {
        kafkaStream = stream;
    }

    public void run() {
        try {
            log.info("start consuming");
            ConsumerIterator<byte[], byte[]> iterator = kafkaStream.iterator();
            Pattern eventPattern = Pattern.compile("(timestamp\":(\\d+))");

            while (iterator.hasNext()) {
                String message = new String(iterator.next().message());
                receivedTime = System.currentTimeMillis();

                //Time stamp pattern match for json format event
                Matcher eventPatternMatcher = eventPattern.matcher(message);
                if (eventPatternMatcher.find()) {
                    sentTime = Long.parseLong(eventPatternMatcher.group(2));
                } else {
                    log.error("unable to extract timestamp from received event");
                }

                latency.addAndGet(receivedTime - sentTime);
                eventCount.incrementAndGet();

                if (eventCount.get() % elapsedCount == 0) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastTime.getAndSet(currentTime);
                    double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                    log.info("Received " + elapsedCount + " sensor events in " + elapsedTime +
                            " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                            " events per second. Average latency is " + (double) latency.get() / elapsedCount + " milliseconds per event.");
                    latency.set(0);
                }
            }
            log.info("Received Total of " + eventCount.get() + " sensor events");
        } catch (Throwable t) {
            log.error("Error when receiving messages", t);
        }
    }
}


