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

package org.wso2.carbon.sample.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.io.*;

public class WSO2EventServerUtil {
    private static Log log = LogFactory.getLog(TestWso2EventServer.class);

    static File securityFile = new File(".." + File.separator + ".." + File.separator + ".." + File.separator
            + "repository" + File.separator + "resources" + File.separator + "security");

    public static void setKeyStoreParams() {
        String keyStore = securityFile.getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", keyStore + "" + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

    }

    public static String getDataBridgeConfigPath() {
        return new File("src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "data-bridge-config.xml").getAbsolutePath();
    }

    public static StreamDefinition loadStream() {
        File fileEntry = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator
                + "org.wso2.event.sensor.stream_1.0.0.json").getAbsoluteFile();
        BufferedReader bufferedReader = null;
        StreamDefinition streamDefinition = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileEntry));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            streamDefinition = EventDefinitionConverterUtils.convertFromJson(stringBuilder.toString().trim());
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
            return streamDefinition;
        }
    }

}
