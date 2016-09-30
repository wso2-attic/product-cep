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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appserver.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import java.rmi.RemoteException;

public class ResourceServiceClient {

    private static final Log log = LogFactory.getLog(ResourceServiceClient.class);
    private final String serviceName = "ResourceAdminService";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private String endPoint;

    public ResourceServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, resourceAdminServiceStub);

    }

    public ResourceServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, resourceAdminServiceStub);
    }

    public void addCollection(String parentPath, String resourceName, String mediaType, String description)
            throws ResourceAdminServiceExceptionException {
        try {
            resourceAdminServiceStub.addCollection(parentPath, resourceName, mediaType, description);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

    public void addTextResource(String parentPath, String resourceName, String mediaType, String description, String content)
            throws ResourceAdminServiceExceptionException {
        try {
            resourceAdminServiceStub.addTextResource(parentPath, resourceName, mediaType, description, content);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("RemoteException", e);
            throw e;
        }
    }

}
