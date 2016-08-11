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

package org.wso2.carbon.sample.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * KakfaUtil class contains utility functions for the Kafka receiver class
 */
public class KafkaUtil {

	private static Log log = LogFactory.getLog(KafkaUtil.class);

	static String sampleDirectoryPath = ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator +
			"samples" + File.separator + "cep" + File.separator + "artifacts" + File.separator + "sampleNumber" + File.separator;

	/**
	 * This method will construct the directory path of the data file
	 *
	 * @param sampleNumber  Number of the sample which is running currently
	 * @param topic         topic of the message to be sent (the data file should be named with the topic)
	 * @param filePath      file path if a sample if not running
	 *
	 */
	public static String getEventFilePath(String sampleNumber, String topic, String filePath)
			throws Exception {
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
			resultingFilePath = sampleDirectoryPath.replace("sampleNumber", sampleNumber) + topic + ".txt";
		} else {
			throw new Exception("In sampleNumber:'" + sampleNumber + "' and filePath:'" + filePath
					+ "' one must be null and other not null");
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
