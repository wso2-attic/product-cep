/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.test.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class JMXAnalyzerClient {

    private static final Log log = LogFactory.getLog(JMXAnalyzerClient.class);

    public static int getThreadCount(String host, String port) throws IOException, MalformedObjectNameException,
            MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        JMXServiceURL url =
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");

        Map<String, String[]> env = new HashMap<String, String[]>();
        String[] credentials = {"admin", "admin"};
        env.put(JMXConnector.CREDENTIALS, credentials);
        int threadCount = 0;
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection mbeanServerConnection = jmxConnector.getMBeanServerConnection();
        final ThreadMXBean remoteThread =
                ManagementFactory.newPlatformMXBeanProxy(
                        mbeanServerConnection,
                        ManagementFactory.THREAD_MXBEAN_NAME,
                        ThreadMXBean.class);
        long[] allthreadIDsArray = remoteThread.getAllThreadIds();

        //get jms thread count
        for (long threadID : allthreadIDsArray) {
            if (remoteThread.getThreadInfo(threadID) != null && remoteThread.getThreadInfo(threadID).getThreadName() != null
                    && remoteThread.getThreadInfo(threadID).getThreadName().startsWith("JMSThreads")) {
                threadCount++;
            }
        }
        //close the connection
        jmxConnector.close();
        return threadCount;
    }

}