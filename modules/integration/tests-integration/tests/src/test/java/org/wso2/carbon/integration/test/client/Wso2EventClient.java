/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.test.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.cep.integration.common.utils.CEPIntegrationTestConstants;

import javax.security.sasl.AuthenticationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.regex.Matcher;

public class Wso2EventClient {

    private static Log log = LogFactory.getLog(Wso2EventClient.class);

    public static void publish(String protocol, String host, String port, String username, String password,
            String streamId,String dataFileName, String testCaseFolderName, StreamDefinition streamDefinition,
            int events, int delay) throws MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
            MalformedURLException, NoStreamDefinitionExistException, AuthenticationException,
            TransportException, SocketException, DataEndpointAgentConfigurationException, DataEndpointException,
            DataEndpointAuthenticationException, DataEndpointConfigurationException {

        String relativeFilePath = getTestDataFileLocation(testCaseFolderName, dataFileName);

        KeyStoreUtil.setTrustStoreParams();
        //create data publisher
        DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username,
                password);

        //Publish event for a valid stream
        publishEvents(dataPublisher, streamDefinition, relativeFilePath, events, delay);
        dataPublisher.shutdown();

    }

    public static String getTestDataFileLocation(String testCaseFolderName, String dataFileName) {

        String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() +
                                  CEPIntegrationTestConstants.RELATIVE_PATH_TO_TEST_ARTIFACTS + testCaseFolderName +
                                  "/" + dataFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        return relativeFilePath;
    }

    private static void publishEvents(DataPublisher dataPublisher, StreamDefinition streamDefinition, String filePath,
            int events, int delay) {

        int metaSize = streamDefinition.getMetaData() == null ? 0 : streamDefinition.getMetaData().size();
        int correlationSize =
                streamDefinition.getCorrelationData() == null ? 0 : streamDefinition.getCorrelationData().size();
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

                Event event = new Event(streamDefinition.getStreamId(), System.currentTimeMillis(), meta, correlation,
                        payload);
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
