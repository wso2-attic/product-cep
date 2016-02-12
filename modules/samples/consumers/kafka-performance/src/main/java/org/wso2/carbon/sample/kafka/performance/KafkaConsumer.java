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

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumer {
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;

    public KafkaConsumer(String zookeeperUrl, String groupId, String topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(zookeeperUrl, groupId));
        this.topic = topic;
    }

    public static void main(String[] args) {
        String zookeeperUrl = args[0];
        String groupId = args[1];
        String topic = args[2];
        int noOfConsumers = Integer.parseInt(args[3]);
        KafkaConsumer kafkaConsumer = new KafkaConsumer(zookeeperUrl, groupId, topic);
        kafkaConsumer.start(noOfConsumers);
    }

    public void start(int numOfConsumers) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(numOfConsumers));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        //Launch all threads
        executor = Executors.newFixedThreadPool(numOfConsumers);

        //Create an object to consume the messages
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream));
        }
    }

    private static ConsumerConfig createConsumerConfig(String zookeeperUrl, String groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeperUrl);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "6000");
        props.put("zookeeper.sync.time.ms", "2000");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }
}


