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

package org.wso2.carbon.integration.test.client;

import org.apache.log4j.Logger;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.*;
import java.util.regex.Matcher;

/**
 * FilePublisherClient client reads a text file with multiple events and post it to the given url.
 */
public class FilePublisherClient {
    private static Logger log = Logger.getLogger(FilePublisherClient.class);

    public static void publish(String destinationFilePath, String testCaseFolderName, String dataFileName) {
        log.info("Starting File EventPublisher Client");
        BufferedWriter bufferedWriter = null;
        BufferedReader bufferedReader = null;

        try {
            String line;
            String sourceFilePath = getTestDataFileLocation(testCaseFolderName, dataFileName);
            bufferedWriter = new BufferedWriter(new FileWriter(destinationFilePath));
            bufferedReader = new BufferedReader(new FileReader(sourceFilePath));

            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

        } catch (FileNotFoundException e) {
            log.error("File " + dataFileName + " is not found", e);
        } catch (IOException e) {
            log.error("Error in reading file " + dataFileName, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when closing the file : " + e.getMessage(), e);
            }
        }
    }

    /**
     * File path will be created for the file to be read with respect to the artifact folder and file name
     *
     * @param testCaseFolderName Artifact folder name
     * @param dataFileName       Text file to be read
     */
    public static String getTestDataFileLocation(String testCaseFolderName, String dataFileName) {
        String relativeFilePath =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "artifacts" + File.separator
                        + "CEP" + File.separator + testCaseFolderName + File.separator
                        + dataFileName;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        return relativeFilePath;
    }


}

