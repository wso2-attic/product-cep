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

package org.wso2.cep.integration.common.utils;

import java.io.File;

public class CEPIntegrationTestConstants {
    public static final String RELATIVE_PATH_TO_TEST_ARTIFACTS = File.separator + "artifacts" + File.separator + "CEP" +
                                                                 File.separator;
    public static final int TCP_PORT = 8461;
    public static final int HTTP_PORT = 10563;
    public static final int HTTPS_PORT = 10243;
    public static final int WIRE_MONITOR_PORT = 10245;
    public static final int WEB_SOCKET_SERVER_PORT = 9899;
    public static final int STORM_WSO2EVENT_SERVER_PORT = 8621;
    public static final int THRIFT_RECEIVER_PORT = 8411;
}
