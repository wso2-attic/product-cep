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
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DataPublisherUtil {

    private static Log log = LogFactory.getLog(DataPublisherUtil.class);

    static File securityFile = new File(".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "resources" + File.separator + "security");
    static String configDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "eventstreams";
    static String sampleDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" + File.separator + "cep" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;
    static String sampleStreamDefinitionsDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" + File.separator + "cep" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator + "eventstreams"  + File.separator;
    static String dataAgentConfigPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "conf" + File.separator + "data-bridge" + File.separator + "data-agent-config.xml";

    public static void setTrustStoreParams() {
        String trustStore = securityFile.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + "" + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    public static void setKeyStoreParams() {
        String keyStore = securityFile.getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", keyStore + "" + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }

    public static String getDataAgentConfigPath() {
        return new File(dataAgentConfigPath).getAbsolutePath();
    }

    public static String getEventFilePath(String sampleNumber, String streamId, String filePath) throws Exception {
        if (sampleNumber != null && sampleNumber.isEmpty() || sampleNumber.equals("\"\"")) {
            sampleNumber = null;
        }

        if (filePath != null && filePath.isEmpty() || filePath.equals("\"\"")) {
            filePath = null;
        }

        String resultingFilePath;
        if (filePath != null && sampleNumber == null) {
            resultingFilePath = filePath;
        } else if (filePath == null && sampleNumber != null) {
            resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber) + streamId.replaceAll(":", "_").replaceAll("\\.", "_") + ".csv";
        } else {
            throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath + "' one must be null and other not null");
        }
        File file = new File(resultingFilePath);
        if (!file.isFile()) {
            throw new Exception("'" + resultingFilePath + "' is not a file");

        }
        if (!file.exists()) {
            throw new Exception("file '" + resultingFilePath + "' does not exist");
        }
        return resultingFilePath;
    }

    public static Map<String, StreamDefinition> loadStreamDefinitions(String sampleNumber) {
        String directoryPath;
        if (sampleNumber.length() != 0) {
            directoryPath = sampleStreamDefinitionsDirectoryPath.replace("sampleNumber", sampleNumber);
        } else {
            directoryPath = configDirectoryPath;
        }
        File directory = new File(directoryPath);
        Map<String, StreamDefinition> streamDefinitions = new HashMap<String, StreamDefinition>();
        if (!directory.exists()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " directory not exist");
            return streamDefinitions;
        }
        if (!directory.isDirectory()) {
            log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " not a directory");
            return streamDefinitions;
        }
        File[] defFiles = directory.listFiles();

        if (defFiles != null) {
            for (final File fileEntry : defFiles) {
                if (!fileEntry.isDirectory()) {


                    BufferedReader bufferedReader = null;
                    StringBuilder stringBuilder = new StringBuilder();
                    try {
                        bufferedReader = new BufferedReader(new FileReader(fileEntry));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(stringBuilder.toString().trim());
                        streamDefinitions.put(streamDefinition.getStreamId(), streamDefinition);
                    } catch (FileNotFoundException e) {
                        log.error("Error in reading file " + fileEntry.getName(), e);
                    } catch (IOException e) {
                        log.error("Error in reading file " + fileEntry.getName(), e);
                    } catch (MalformedStreamDefinitionException e) {
                        log.error("Error in converting Stream definition " + e.getMessage(), e);
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
            }
        }

        return streamDefinitions;

    }

}
