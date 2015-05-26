/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.performance;

import org.apache.log4j.Logger;

import java.io.*;

public class DataPublisherUtil {

    private static Logger log = Logger.getLogger(DataPublisherUtil.class);

    static File securityFile = new File(".." + File.separator + ".." + File.separator + ".." + File.separator
            + "repository" + File.separator + "resources" + File.separator + "security");
    static String dataAgentConfigPath = ".." + File.separator + ".." + File.separator + ".." + File.separator
            + "repository" + File.separator + "conf" + File.separator + "data-bridge" + File.separator
            + "data-agent-config.xml";

    public static void setTrustStoreParams() {
        String trustStore = securityFile.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + "" + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    public static String getDataAgentConfigPath() {
        return new File(dataAgentConfigPath).getAbsolutePath();
    }

}
