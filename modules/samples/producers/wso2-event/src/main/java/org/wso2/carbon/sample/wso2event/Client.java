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

package org.wso2.carbon.sample.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class Client {
    private static Log log = LogFactory.getLog(Client.class);

    public static void main(String[] args) {

        System.out.println(Arrays.deepToString(args));
        try {
            System.out.println("Starting WSO2 Event Client");

            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath());
            DataPublisherUtil.setTrustStoreParams();

            String protocol = args[0];
            String host = args[1];
            String port = args[2];
            String username = args[3];
            String password = args[4];
            String streamId = args[5];
            String sampleNumber = args[6];
            String filePath = args[7];
            int events = Integer.parseInt(args[8]);
            int delay = Integer.parseInt(args[9]);

            Map<String, StreamDefinition> streamDefinitions = DataPublisherUtil.loadStreamDefinitions(sampleNumber);
            if (streamId == null || streamId.length() == 0) {
                throw new Exception("streamId not provided");
            }
            StreamDefinition streamDefinition = streamDefinitions.get(streamId);
            if (streamDefinition == null) {
                throw new Exception("StreamDefinition not available for stream " + streamId);
            } else {
                log.info("StreamDefinition used :" + streamDefinition);
            }

            filePath = DataPublisherUtil.getEventFilePath(sampleNumber, streamId, filePath);

            //create data publisher
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username, password);


            //Publish event for a valid stream
            publishEvents(dataPublisher, streamDefinition, filePath, events, delay);

            dataPublisher.shutdownWithAgent();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, StreamDefinition streamDefinition, String filePath, int events, int delay) {

        int metaSize = streamDefinition.getMetaData() == null ? 0 : streamDefinition.getMetaData().size();
        int correlationSize = streamDefinition.getCorrelationData() == null ? 0 : streamDefinition.getCorrelationData().size();
        int payloadSize = streamDefinition.getPayloadData() == null ? 0 : streamDefinition.getPayloadData().size();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null && events != 0) {
                String[] data = line.split(",");
                Object[] meta = null;
                if (metaSize > 0) {
                    meta = new Object[metaSize];
                    java.util.List<Attribute> metaData = streamDefinition.getMetaData();
                    for (int i = 0; i < metaData.size(); i++) {
                        Attribute attribute = metaData.get(i);
                        meta[i] = convertData(data[i], attribute);
                    }
                }

                Object[] correlation = null;
                if (correlationSize > 0) {
                    correlation = new Object[correlationSize];
                    java.util.List<Attribute> correlationData = streamDefinition.getCorrelationData();
                    for (int i = 0; i < correlationData.size(); i++) {
                        Attribute attribute = correlationData.get(i);
                        correlation[i] = convertData(data[metaSize + i], attribute);
                    }
                }

                Object[] payload = null;
                if (payloadSize > 0) {
                    payload = new Object[payloadSize];
                    java.util.List<Attribute> payloadData = streamDefinition.getPayloadData();
                    for (int i = 0; i < payloadData.size(); i++) {
                        Attribute attribute = payloadData.get(i);
                        payload[i] = convertData(data[metaSize + correlationSize + i], attribute);
                    }
                }

                Event event = new Event(streamDefinition.getStreamId(), System.currentTimeMillis(), meta, correlation, payload);
                dataPublisher.publish(event);

                if (delay > 0) {
                    Thread.sleep(delay);
                }
                events--;
            }
        } catch (FileNotFoundException e) {
            log.error("Error in reading file " + filePath, e);
        } catch (IOException e) {
            log.error("Error in reading file " + filePath, e);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while sleeping between events", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }

    }

    private static Object convertData(String data, Attribute attribute) {
        switch (attribute.getType()) {
            case INT:
                return Integer.parseInt(data);
            case LONG:
                return Long.parseLong(data);
            case FLOAT:
                return Float.parseFloat(data);
            case DOUBLE:
                return Double.parseDouble(data);
            case STRING:
                return data;
            case BOOL:
                return Boolean.parseBoolean(data);
        }
        throw new RuntimeException("data:" + data + " is of unsupported type");
    }


}
