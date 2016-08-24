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

package org.wso2.carbon.sample.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WSO2EventServerUtil {
    private static Log log = LogFactory.getLog(TestWso2EventServer.class);

    static File securityFile = new File(".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "resources" + File.separator + "security");
    static String configDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "eventstreams";
    static String sampleDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" + File.separator + "cep" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator + "eventstreams";

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

    public static String getDataBridgeConfigPath() {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("src" + File.separator + "main" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-bridge-config.xml";
    }

    public static List<StreamDefinition> loadStreamDefinitions(String sampleNumber) {
        String directoryPath;
        if (sampleNumber.length() != 0) {
            directoryPath = sampleDirectoryPath.replace("sampleNumber", sampleNumber);
        } else {
            directoryPath = configDirectoryPath;
        }
        File directory = new File(directoryPath);
        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
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
                        streamDefinitions.add(streamDefinition);
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
