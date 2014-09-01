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
package org.wso2.carbon.cep.sample.client.util;

public class DataProvider {
    private static final int MULTIPLIER = 14;

    public static Object[] getPayload() {
        int userIndex = Math.round((float) Math.random() * MULTIPLIER);
        int termIndex = Math.round((float) Math.random() * MULTIPLIER);
        return new Object[]{SamplingDataSet.USER_IDS.get(userIndex), SamplingDataSet.SEARCH_TERMS.get(termIndex)};
    }

    public static String getMeta() {
        int ipIndex = Math.round((float) Math.random() * MULTIPLIER);
        return SamplingDataSet.IP_ADDRESSES.get(ipIndex);
    }
}
