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

package org.wso2.carbon.sample.http;

import java.io.File;

/**
 * HttpUtil class contains utility functions for the Http receiver class
 */
public class HttpUtil {

	static File securityFile = new File(
			".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "repository" +
			File.separator + "resources" + File.separator + "security");
	static String sampleFilPath =
			".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "samples" +
			File.separator + "cep" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;
	static String fileExtension = ".txt";

	public static void setTrustStoreParams() {
		String trustStore = securityFile.getAbsolutePath();
		System.setProperty("javax.net.ssl.trustStore",
		                   trustStore + "" + File.separator + "client-truststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
	}

	/**
	 * File path will be created for the file to be read with respect to the arguments passed. If sample number given file path will be created accordingly
	 *
	 * @param filePath     Text file to be read
	 * @param sampleNumber Number of the http sample
	 */
	public static String getMessageFilePath(String sampleNumber, String filePath, String url) throws Exception {
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
			String urlSplitter[] = url.split("/");
			resultingFilePath = sampleFilPath.replace("sampleNumber", sampleNumber)+urlSplitter[urlSplitter.length-1]+fileExtension;
		} else {
			throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath +
			                    "' either one should be null");
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
