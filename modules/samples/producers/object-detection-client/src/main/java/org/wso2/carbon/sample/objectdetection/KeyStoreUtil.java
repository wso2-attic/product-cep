/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sample.objectdetection;

import java.io.File;

/**
 * Class to add key store and trust store parameters to system properties.
 */
public class KeyStoreUtil {

    /**
     * The file path to trust store and key store.
     */
    private static File filePath = new File("src/main/resources/store");

    /**
     * Sets the trust store values to system properties.
     */
    public static void setTrustStoreParams() {
        String trustStore = filePath.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    /**
     * Sets the key store values to system properties.
     */
    public static void setKeyStoreParams() {
        String keyStore = filePath.getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", keyStore + "/wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }
}
