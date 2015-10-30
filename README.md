<!--
 ~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
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

WSO2 Complex Event Processor (CEP)
==================================

---

| Branch | Build Status |
| :------------ |:-------------
| master | [![Build Status](https://wso2.org/jenkins/job/product-cep/badge/icon)](https://wso2.org/jenkins/job/product-cep) |

---

Latest Released Version 4.0.0.

Welcome to the WSO2 Complex Event Processor.

WSO2 CEP is a lightweight, easy-to-use, open source Complex Event Processing server. It identifies the most meaningful events within the event cloud, analyzes their impact, and acts on them in real-time. It's built to be extremely high performing with WSO2 Siddhi and massively scalable using Apache Storm. CEP can be tightly integrated with WSO2 Data Analytics Server, by adding support for recording and post processing events with Map-Reduce via Apache Spark, and WSO2 Machine Learner for predictive analytics.

##Features

####Process data in motion, and analyze in real time.
* Powerful, extensible language for stream processing, powered by WSO2 Siddhi.
* SQL like query language providing intuitive and complex event processing support.
* Supports filtering by conditions, the ability to create and join event streams, execute temporal queries using various windows, event tables and partitions, and identifies sequences of event occurrences.
* Visualize, monitor and act in real time

####Template based, configuration driven execution plan design
* Event tracing, try-it, event flow visualization and event simulation capabilities
* Built-in collection and monitoring of standard access and performance statistics
* Effective real time dashboard with support for gadget generation
* Connect and integrate with anything.

####Support for many transport receivers, including for the Internet of Things (IoT)
* Data publisher agents to plug into enterprise systems
* Easily extend for scalability and high availability

####Extensible use case driven toolbox support including fraud detection, time series, geo fencing and natural language processing plugin.
* Supports high availability deployment.
* Supports distributed event processing with Apache Storm and Siddhi.
* Supports long running queries via persistence support

System Requirements
-------------------

1. Minimum memory - 2GB
2. Java 1.7 or higher
3. The Management Console does not support MS IE.
4. To compile and run the sample clients, an Ant version is required. Ant 1.7.0 version is recommended
5. To build WSO2 CEP from the Source distribution, it is necessary that you have JDK 1.7 version or later and Maven 3.0.4 or later

For more details see http://docs.wso2.com/display/CEP400/Installation+Prerequisites

Installation & Running
----------------------
1. Download the WSO2 CEP from http://wso2.com/products/complex-event-processor/
2. Extract the downloaded zip file
3. Run the wso2server.sh or wso2server.bat file in the bin directory
4. Once the server starts, point your Web browser to https://localhost:9443/carbon/
5. Use the following username and password to login
    username : admin
    password : admin

For more details see https://docs.wso2.com/display/CEP400/Installing+the+Product

Running Samples
---------------
WSO2 CEP server can be started with the sample mode
To start with sample mode run ./wso2cep-samples.sh -sn <sampleNo> or wso2cep-samples.bat -sn <sampleNo> in the bin directory
Example, to run sample 0001: ./wso2cep-samples.sh -sn 0001

For more details see https://docs.wso2.com/display/CEP400/Samples+Guide .

**For further details, see the WSO2 Complex Event Processor documentation at http://docs.wso2.com/complex-event-processor**

## Building the product 
If snapshot versions of dependent jars are not available or out of date, build the following repositories first before building this repository .

* siddhi - https://github.com/wso2/siddhi
* carbon-dashboards - https://github.com/wso2/carbon-dashboards
* carbon-analytics-common - https://github.com/wso2/carbon-analytics-common
* carbon-event-processing - https://github.com/wso2/carbon-event-processing

## How to Contribute
* Please report issues at [CEP JIRA] (https://wso2.org/jira/browse/CEP).
* Send your bug fixes pull requests to [master branch] (https://github.com/wso2/product-cep/tree/master) 

## Contact us
WSO2 Carbon developers can be contacted via the mailing lists:

* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org



