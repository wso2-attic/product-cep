/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appserver.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub;
import org.wso2.carbon.event.simulator.stub.types.EventDto;

import java.rmi.RemoteException;

public class EventSimulatorAdminServiceClient {
    private static final Log log = LogFactory.getLog(EventSimulatorAdminServiceClient.class);
    private final String serviceName = "EventSimulatorAdminService";
    private EventSimulatorAdminServiceStub executionSimulatorAdminServiceStub;
    private String endPoint;

    public EventSimulatorAdminServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        executionSimulatorAdminServiceStub = new EventSimulatorAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, executionSimulatorAdminServiceStub);
    }
    public EventSimulatorAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        executionSimulatorAdminServiceStub = new EventSimulatorAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, executionSimulatorAdminServiceStub);
    }

    public ServiceClient _getServiceClient() {
        return executionSimulatorAdminServiceStub._getServiceClient();
    }

    public void sendEvent(EventDto eventDto) throws RemoteException {
        try {
            executionSimulatorAdminServiceStub.sendEvent(eventDto);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException();
        }
    }

}
