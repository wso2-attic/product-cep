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
package org.wso2.carbon.cep.sample.client;

import java.io.*;

public class KeyStoreUtil {

    public static void setTrustStoreParams() {
        ClassLoader classLoader = KeyStoreUtil.class.getClassLoader();
        String storeName = "client-truststore.jks";
        String filePath = KeyStoreUtil.writeInputStreamToFile(classLoader.getResourceAsStream(storeName), storeName);
        System.setProperty("javax.net.ssl.trustStore", filePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

    }

    public static void setKeyStoreParams() {
        ClassLoader classLoader = KeyStoreUtil.class.getClassLoader();
        String storeName = "wso2carbon.jks";
        String filePath = KeyStoreUtil.writeInputStreamToFile(classLoader.getResourceAsStream(storeName), storeName);
        System.setProperty("Security.KeyStore.Location", filePath);
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

    }

    public static String writeInputStreamToFile(InputStream inputStream, String filename) {

        OutputStream outputStream = null;
        File outFile = new File(filename);
        try {
            // write the inputStream to a FileOutputStream
            outputStream =
                    new FileOutputStream(outFile);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return outFile.getAbsolutePath();
    }

}
