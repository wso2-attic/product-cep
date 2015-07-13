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

import com.google.gson.JsonObject;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class Kafka {
    private static Logger log = Logger.getLogger(Kafka.class);

    private static String url;
    private static String topic;
    private static long noOfEvents;
    private AtomicLong count;
    private static int elapsedCount;
    private static int noOfPublishers;

    public Kafka() {
        count = new AtomicLong(0);
    }

    public static void main(String args[]) {
        log.info("Starting Kafka Client");
        url = args[0];
        topic = args[1];
        try {
            noOfEvents = Long.parseLong(args[2]);
            elapsedCount = Integer.parseInt(args[3]);
            noOfPublishers = Integer.parseInt(args[4]);

            Kafka kafka = new Kafka();
            kafka.start();
        } catch (NumberFormatException e) {
            log.error("Entered value for no of events is invalid. Please enter an integer", e);
        }
    }

    private void start() {
        ExecutorService executor = Executors.newFixedThreadPool(noOfPublishers);
        for (int i = 0; i < noOfPublishers; i++) {
            executor.execute(new KafkaProducer());
        }
    }

    public class KafkaProducer implements Runnable {

        @Override
        public void run() {
            try {
                Properties props = new Properties();
                props.put("metadata.broker.list", url);
                props.put("serializer.class", "kafka.serializer.StringEncoder");

                ProducerConfig config = new ProducerConfig(props);
                Producer<String, Object> producer = new Producer<String, Object>(config);

                log.info("Sending messages..");
                long lastTime = System.currentTimeMillis();
                DecimalFormat decimalFormat = new DecimalFormat("#");
                while (count.getAndIncrement() < noOfEvents) {
                    String message = "{\"event\": " + getRandomEvent(count.get()).toString() + "}";

                    KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic, message);
                    producer.send(data);

                    if (count.get() % elapsedCount == 0) {
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastTime;
                        double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                        lastTime = currentTime;
                        log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime +
                                " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                                " events per second.");
                    }
                }
            } catch (Throwable t) {
                log.error("Error when sending the messages", t);
            }
        }

        private JsonObject getRandomEvent(long count) {

            JsonObject event = new JsonObject();

            JsonObject metaData = new JsonObject();
            JsonObject correlationData = new JsonObject();
            JsonObject payLoadData = new JsonObject();

            metaData.addProperty("timestamp", System.currentTimeMillis());
            metaData.addProperty("isPowerSaverEnabled", false);
            metaData.addProperty("sensorId", count);
            metaData.addProperty("sensorName", "temperature");

            correlationData.addProperty("longitude", 2332.424);
            correlationData.addProperty("latitude", 2323.23232);

            payLoadData.addProperty("humidity", 2.3f);
            payLoadData.addProperty("sensorValue", 23423.234);

            event.add("metaData", metaData);
            event.add("correlationData", correlationData);
            event.add("payloadData", payLoadData);

            return event;
        }
    }


}

