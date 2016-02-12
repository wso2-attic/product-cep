/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Memory
var minMaxAverageRespTimesGraph;

function initStats(memoryXScale) {
    if (memoryXScale != null) {
        initReqCountGraphs(memoryXScale);
    } else {
        initReqCountGraphs(30);
    }
}

function isNumeric(sText){
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber == true; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initReqCountGraphs(memoryXScale) {
    if (memoryXScale < 1 || !isNumeric(memoryXScale)) {
        return;
    }
    minMaxAverageRespTimesGraph = new minMaxAvgGraph(memoryXScale);
}

