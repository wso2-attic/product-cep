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

/*This js mainly concern with the data structures related to graphs*/
function carbonGraph(xscale) {
    this.array = new Array();
    for (var i = 0; i < xscale; i++) {
        this.array[i] = [i, 0.0];
    }
    this.xscale = xscale;
}

function getData() {
    return this.array;
}

function addData(newValue) {

    //shift to left
    for (var i = 0; i < this.xscale - 1; i++) {
        this.array[i] = [i,this.array[i + 1][1]];  // (x,y)
    }

    //add the value to the last postion
    this.array[this.xscale - 1] = [this.xscale - 1,newValue];
}

function graphTickGenerator() {
    var tickArray = [];
    var startTick = 10;
    var i = startTick - 1;
    var weight = this.xscale / 10;
    do {
        var t = (startTick - i) * weight - 1;
        var v = i * weight;
        if (v == 0) {
            v = "0";
        }
        tickArray.push([t, v]);
        i--;
    } while (i > -1);
    return tickArray;
}

carbonGraph.prototype.get = getData;
carbonGraph.prototype.add = addData;
carbonGraph.prototype.tick = graphTickGenerator;