#!/bin/sh

#
# Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# ----------------------------------------------------------------------------
# Script for runnig the WSO2 CEP Server samples
#
# Environment Variable Prequisites
#
#   CARBON_HOME   Home of WSO2 Carbon installation. If not set I will  try
#                   to figure it out.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
#   JAVA_OPTS       (Optional) Java runtime options used when the commands
#                   is executed.
#
# NOTE: Borrowed generously from Apache Tomcat startup scripts.
# -----------------------------------------------------------------------------

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# ----- Process the input command ----------------------------------------------
CMD=""
SAMPLE=""
VALIDATE=""
for c in $*
do
  if [ "$c" = "-sn" ] || [ "$c" = "sn" ]; then
    SAMPLE="t"
    VALIDATE="t"
    continue
  elif [ "$SAMPLE" = "t" ]; then
    NODIGITS="$(echo $c | sed 's/[[:digit:]]//g')"
    if [ -z $NODIGITS ]; then
      SAMPLE=""
      CARBON_HOME=`cd "$PRGDIR/.." ; pwd`
      CMD="$CMD -Daxis2.repo=$CARBON_HOME/samples/artifacts/$c"
      `[ -f $CARBON_HOME/samples/artifacts/$c/stream-definitions.xml ] && cp $CARBON_HOME/samples/artifacts/$c/stream-definitions.xml $CARBON_HOME/repository/conf/data-bridge`
    else
      echo "*** Specified sample number is not a number *** Please specify a valid sample number with the -sn option"
      echo "Example, to run sample 1: wso2cep-samples.sh -sn 1"
      exit
    fi
  else
    CMD="$CMD $c"
  fi
done

if [ -z $VALIDATE ]; then
  echo "*** Sample number to be started is not specified *** Please specify a sample number to be started with the -sn option"
  echo "Example, to run sample 1: wso2cep-samples.sh -sn 1"
  exit
fi

sh $PRGDIR/wso2server.sh$CMD
