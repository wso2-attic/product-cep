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
package org.wso2.carbon.sample.sensorstats;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

public class SensorStatsProducer {
    public static void main(String[] args) {
        String topic;
        String url;

        if (args.length == 0) {
            url = "localhost:9092";
            topic = "sensorStream";
        } else {
            url = args[0];
            topic = args[1];
        }

        String[] sensorEvents = new String[]{
                "<events>\n" +
                "    <event>\n" +
                "        <payloadData>\n" +
                "            <sensorId>ID1</sensorId>\n" +
                "            <sensorVersion>version1</sensorVersion>\n" +
                "            <sensorValue>45</sensorValue>\n" +
                "        </payloadData>\n" +
                "    </event>\n" +
                "</events>",
                "<events>\n" +
                "    <event>\n" +
                "        <payloadData>\n" +
                "            <sensorId>ID2</sensorId>\n" +
                "            <sensorVersion>version2</sensorVersion>\n" +
                "            <sensorValue>43</sensorValue>\n" +
                "        </payloadData>\n" +
                "    </event>\n" +
                "</events>",
                "<events>\n" +
                "    <event>\n" +
                "        <payloadData>\n" +
                "            <sensorId>ID1</sensorId>\n" +
                "            <sensorVersion>version3</sensorVersion>\n" +
                "            <sensorValue>23</sensorValue>\n" +
                "        </payloadData>\n" +
                "    </event>\n" +
                "</events>"
        };

        Properties props = new Properties();
        props.put("metadata.broker.list", url);
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, Object> producer = new Producer<String, Object>(config);

        for (String sensorEvent : sensorEvents) {
            KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic, sensorEvent);
            producer.send(data);
        }

        producer.close();
    }
}
