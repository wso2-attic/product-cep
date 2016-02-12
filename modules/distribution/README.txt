================================================================================
                        WSO2 Complex Event Processing Server 4.1.0
================================================================================

Welcome to the WSO2 CEP 4.1.0 release

WSO2 CEP is a lightweight and easy-to-use Open Source Complex Event Processing
Server (CEP) is available under the Apache Software License v2.0. WSO2 Complex
Event Processor identifies the most meaningful events within the event cloud,
analyzes their impacts, and acts on them in real time. Its built to be extremely
high performing and massively scalable.

This is based on the revolutionary WSO2 Carbon framework. All the major features
have been developed as pluggable Carbon components.

New Features In This Release
==================================

[CEP-101] - Indexing for Hazelcast table
[CEP-105] - Time length window implementation for Siddhi
[CEP-644] - add insertOrUpdate to event table
[CEP-1365] - Add a function to extract 'DAY OF WEEK' from time/timestamp
[CEP-1423] - Support outer/left joins in Siddhi
[CEP-1439] - Map extension support
[CEP-1440] - Geo Closest function
[CEP-1441] - External Batch window for siddhi
[CEP-1448] - Hazelcast Event Table for Siddhi
[CEP-1457] - Minima and Maxima detection on Siddhi
[CEP-1459] - Instrumentation & Monitoring support for CEP and Siddhi

Key Features of WSO2 CEP
==================================
* Support Multiple Input Event Adapter Types - WSO2 CEP supports WSO2Event, SOAP, HTTP, JMS, Email, File-tail, Kafka, MQTT, Websocket input adapter types
* Support Multiple Output Event Adapter Types - WSO2 CEP supports WSO2Event, SOAP, HTTP, JMS, Email, SMS, Logger, Kafka, RDBMS, Cassandra, MQTT, Websocket, UI output adapter types
* Support for multiple event formats such as XML, JSON, Map, Text & WSO2Event
* Distributed processing support - WSO2 CEP can be integrated with Apache Storm to perform distributed processing
* Siddhi Query Partitioning
* Siddhi Event Tables to use historical data within real time processing
* High Availability Deployment
* Output rate limiting support for Siddhi
* Monitoring Support - WSO2 CEP supports system, per execution plan/per deployment artifact monitoring.
* Tracing Support - WSO2 CEP supports tracing a message at multiple stages of the event flow.
* Event Flow Support - WSO2 CEP supports visualizing the event flow within the system.
* Event Simulator - WSO2 CEP supports an easy gui to send sample events to test the event flow.
* Siddhi Try It Tool - A tool for experimenting with Siddhi Query Language.
* Execution Manager - A user friendly dashboard to customize preconfigured domain-specific execution parameters.
* Analytics Dashboard - An inbuilt dashboard, capable of visualizing event streams in real-time
* Easily Integrates with Enterprise System - by RESTful HTTP protocol with JSON, JMS MAP/XML/Text messages, SOAP and Email
* Support for Long Duration Queries - Supports periodic snapshots to a scalable persistence store.
* Tightly Integrates with WSO2 Data Analytics Server - for recording and post processing of events.
* Realtime Dashboard using the inbuilt Realtime Analytics Gadgets
* Geo Dashboard with geo fencing capabilities
* Siddhi Extensions
* Siddhi query autocompletion
* Event Adaptor Extension
* Configuration based class loading mechanism for Siddhi
* Arbitrary key values pair support for WSO2 Events

Installation & Running
==================================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

4. Use the following username and password to login

    username : admin
    password : admin

You can use the carbon Feature management Console and add the back end runtime features to CEP.
For more information on installation read INSTALL.txt.
   

WSO2 CEP 4.1.0 Binary Distribution Directory Structure
==========================================================


     CARBON_HOME
        |-- bin <directory>
        |-- dbscripts <directory>
        |-- lib <directory>
        |-- client-lib
        |-- repository <directory>           |
        |   |-- components <directory>
        |   |-- conf <directory>
        |   |-- data <directory>
        |   |-- database <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
        |   |-- resources <directory>
        |   |   -- security <directory>
        |   `-- tenants <directory>
        |-- modules <directory>
        |-- resources <directory>
        |-- webapp-mode <directory>
        |-- tmp <directory>
	    |-- samples <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        `-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to start-up  WSO2 CEP
      in standalone mode

    - client-lib
          Contains the basic set of libraries required to run the Data-bridge clients.

    - repository
      The repository where services and modules deployed in WSO2 CEP
      are stored.

        - carbonapps
          Carbon Application hot deployment directory.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

        - data
          Contains internal LDAP related data.

        - database
          Contains the database

        - deployment
          Contains server side and client side Axis2 repositories.
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

	    - tenants
	      Directory will contain relevant tenant artifacts
	      in the case of a multitenant deployment.

    -modules
      contains the modules that needed for UES dashboard.

    -resources
      contains the resources like themes.

    - webapp-mode
      The user has the option of running WSO2 Carbon in webapp mode (hosted as a web-app in an application server).
            This directory contains files required to run Carbon in webapp mode.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 CEP is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 CEP

    - release-notes.html
      Release information for WSO2 CEP 4.1.0


Secure sensitive information in carbon configuration files
==============================================================

There are sensitive information such as passwords in the carbon configuration.
You can secure them by using secure vault. Please go through following steps to
secure them with default mode.

1. Configure secure vault with default configurations by running ciphertool
	script from bin directory.

> ciphertool.sh -Dconfigure   (in UNIX)

This script would do following configurations that you need to do by manually

(i) Replaces sensitive elements in configuration files,  that have been defined in
		 cipher-tool.properties, with alias token values.
(ii) Encrypts plain text password which is defined in cipher-text.properties file.
(iii) Updates secret-conf.properties file with default keystore and callback class.

cipher-tool.properties, cipher-text.properties and secret-conf.properties files
			can be found at repository/conf/security directory.

2. Start server by running wso2server script from bin directory

> wso2server.sh   (in UNIX)

By default mode, it would ask you to enter the master password
(By default, master password is the password of carbon keystore and private key)

3. Change any password by running ciphertool script from bin directory.

> ciphertool -Dchange  (in UNIX)

For more details see
https://docs.wso2.com/display/Carbon441/Encrypting+Passwords+with+Cipher+Tool


Training
==================================

WSO2 Inc. offers a variety of professional Training Programs, including
training on general Web services as well as WSO2 CEP, Siddhi, WSO2 DAS,
Data Services and a number of other products.

For additional support information please refer to
http://wso2.com/training/

Support
==================================

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 CEP, visit the WSO2 Oxygen Tank (http://wso2.com)


Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/CEP



Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/


For further details, see theWSO2 Complex Event Processor documentation at
https://docs.wso2.com/display/CEP410/WSO2+Complex+Event+Processor+Documentation

---------------------------------------------------------------------------
(c) Copyright 2016 WSO2 Inc.