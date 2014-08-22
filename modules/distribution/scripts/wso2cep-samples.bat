@echo off

REM Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM WSO2 Inc. licenses this file to you under the Apache License,
REM Version 2.0 (the "License"); you may not use this file except
REM in compliance with the License.
REM You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.

rem ---------------------------------------------------------------------------
rem Main Script for WSO2 Carbon
rem
rem Environment Variable Prequisites
rem
rem   CARBON_HOME   Home of CARBON installation. If not set I will  try
rem                   to figure it out.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the commands
rem                   is executed.
rem ---------------------------------------------------------------------------

SET cn=1

set CMD=%*

:initial
if "%1"=="-sn" goto sname
if "%1"=="" goto no_sample
shift
goto initial

:sname
shift
set cn=%1
if "%1"=="" goto invalid_number
if "%1"=="0" goto run
SET /A UserInputVal="%cn%"*1
IF %UserInputVal% EQU 0 GOTO invalid_number
goto copyfile

:lreturn
shift
goto initial

:run
wso2server.bat %CMD% -Daxis2.repo=../samples/artifacts/%cn%
goto done

:copyfile
if exist ..\samples\artifacts\%cn%\stream-definitions.xml copy ..\samples\artifacts\%cn%\stream-definitions.xml ..\repository\conf\data-bridge > nul
goto run

:invalid_number
echo "*** Specified sample number is not a number *** Please specify a valid sample number with the -sn option"
echo "Example, to run sample 1: wso2cep-samples.bat -sn 1"
goto done

:no_sample
echo "*** Sample number to be started is not specified *** Please specify a sample number to be started with the -sn option"
echo "Example, to run sample 1: wso2cep-samples.bat -sn 1"
goto done

:done