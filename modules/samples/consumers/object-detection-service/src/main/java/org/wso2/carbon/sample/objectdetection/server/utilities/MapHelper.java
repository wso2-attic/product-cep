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
package org.wso2.carbon.sample.objectdetection.server.utilities;

import java.util.TreeMap;

import org.wso2.carbon.sample.objectdetection.server.data.StreamData;

/**
 * The Map that stores stream data collected by the HTTP servlet
 */
public class MapHelper {

	/**
	 * The map that stores streamed data.
	 */
	private static TreeMap<Integer, StreamData> streamDataTreeMap =
																new TreeMap<Integer, StreamData>();

	/**
	 * Gets the object frames map.
	 *
	 * @return the object frame map
	 */
	public static TreeMap<Integer, StreamData> getStreamDataTreeMap() {
		return streamDataTreeMap;
	}
}
