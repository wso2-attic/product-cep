<!--
~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  WSO2 Inc. licenses this file to you under the Apache License,
~  Version 2.0 (the "License"); you may not use this file except
~  in compliance with the License.
~  You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

This sample uses
InputEventAdaptor:  wso2event
EventBuilder:       wso2event
EventFormatter:     http
OutputEventAdaptor: http

Producers:     video-frame-client and object-detection-client
Consumers:     object-detection-service

Note : Please add the following jar files to the <CEP_HOME>/samples/lib/ folder.
- http://maven.wso2.org/nexus/content/groups/wso2-public/commons-httpclient/wso2/commons-httpclient/3.1.0.wso2v2/commons-httpclient-3.1.0.wso2v2.jar
- http://central.maven.org/maven2/commons-lang/commons-lang/1.0.1/commons-lang-1.0.1.jar
- http://central.maven.org/maven2/javax/servlet/javax.servlet-api/3.0.1/javax.servlet-api-3.0.1.jar
- http://central.maven.org/maven2/nu/pattern/opencv/2.4.9-4/opencv-2.4.9-4.jar