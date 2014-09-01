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
package org.wso2.carbon.sample.flightstats;

import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIContext {
    private InitialContext initContext = null;
    private QueueConnectionFactory queueConnectionFactory = null;
    public static JNDIContext instance = null;

    private JNDIContext() {
        createInitialContext();
        createConnectionFactory();
    }

    public InitialContext getInitContext() {
        return initContext;
    }

    public QueueConnectionFactory getQueueConnectionFactory() {
        return queueConnectionFactory;
    }

    public static JNDIContext getInstance() {
        if (instance == null) {
            instance = new JNDIContext();
        }
        return instance;
    }


    /**
     * Create Connection factory with initial context
     */
    private void createConnectionFactory() {

        // create connection factory
        try {
            queueConnectionFactory = (QueueConnectionFactory) initContext.lookup("ConnectionFactory");
        } catch (NamingException e) {
            System.out.println("Can not create queue connection factory." + e);
        }
    }


    /**
     * Create Initial Context with given configuration
     */
    private void createInitialContext() {

        try {
            initContext = new InitialContext();
        } catch (NamingException e) {
            System.out.println("Can not create initial context with given parameters." + e);
        }
    }
}