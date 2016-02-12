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

package org.wso2.carbon.sample.websocket;

import java.io.File;

/**
 * Util class contains utility functions for the Http receiver class
 */
public class Util {

    static File securityFile = new File(
            ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" +
            File.separator + "resources" + File.separator + "security");
    static String sampleFilPath =
            ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" +
            File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;
    static String fileExtension = ".txt";


    /**
     * File path will be created for the file to be read with respect to the arguments passed. If sample number given file path will be created accordingly
     *
     * @param sampleNumber Number of the http sample
     * @param filePath     Text file to be read
     * @param url          URL of the Websocket server connecting to (in client mode).
     */
    public static String getMessageFilePath(String sampleNumber, String filePath, String url)
            throws Exception {
        if (sampleNumber != null && sampleNumber.length() == 0) {
            sampleNumber = null;
        }

        if (filePath != null && filePath.length() == 0) {
            filePath = null;
        }

        String resultingFilePath;
        if (filePath != null && sampleNumber == null) {
            resultingFilePath = filePath;
        } else if (filePath == null && sampleNumber != null) {
            if (url.isEmpty()) {
                resultingFilePath = sampleFilPath.replace("sampleNumber", sampleNumber) + "Events" + fileExtension;
            } else {
                String urlSplitter[] = url.split("/");
                resultingFilePath = sampleFilPath.replace("sampleNumber", sampleNumber) + urlSplitter[urlSplitter.length - 1] + fileExtension;
            }
        } else {
            throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath +
                                "' only one should be null");
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
}
