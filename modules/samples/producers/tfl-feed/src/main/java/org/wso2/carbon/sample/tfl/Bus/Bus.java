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

package org.wso2.carbon.sample.tfl.Bus;

import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.TflStream;

import java.util.HashMap;
import java.util.PriorityQueue;

public class Bus {
	String id;
	double longitude = 0;
	double latitude = 0;
	double speed = 0.0;
	double angle = 0.0;
	BusStop lastStop;
	PriorityQueue<Prediction> predictions;
	HashMap<BusStop, Prediction> predictionsMap;

	public Bus(String id) {
		this.id = id;
		predictions = new PriorityQueue<Prediction>();
		predictionsMap = new HashMap<BusStop, Prediction>();
	}

	public void setData(BusStop bt, long time) {
		Prediction p = predictionsMap.get(bt);
		if (p == null) {
			p = new Prediction(bt, time + TflStream.timeOffset);
			predictionsMap.put(bt, p);
			predictions.add(p);
		} else {
			p.time = time + TflStream.timeOffset;
		}
	}

	public void setNew() {
		Prediction p = predictions.peek();
		if (p != null) {
			this.latitude = p.busStop.latitude;
			this.longitude = p.busStop.longitude;
			lastStop = p.busStop;
		}
	}

	public String move(long from, long period) {
		Prediction p = predictions.peek();
		if(p == null) return null;

		while(p.time < from) {
			predictions.poll();
			p = predictions.peek();
			if(p == null) return null;
		}
		
		if (p.busStop == lastStop && (p.busStop.latitude != this.latitude ||
    		 p.busStop.longitude != this.longitude)) {
    		 predictions.poll();
    		 return move(from, period);
		}
		if (p.time < from + period) {
			this.latitude = p.busStop.latitude;
			this.longitude = p.busStop.longitude;
			lastStop = p.busStop;
			period = from + period - p.time;
			from = p.time;
			predictions.poll();
			p = predictions.peek();
			if (p == null) return null;
		}

		double newLatitude=
		                ((this.latitude * (p.time - from - period) + p.busStop.latitude * (period)) / (p.time -
		                                                                                               from + 0.0));
		double newLongitude =
		                 ((this.longitude * (p.time - from - period) + p.busStop.longitude *
		                                                               (period)) / (p.time - from + 0.0));
		
		angle = Math.atan2(newLongitude - longitude, newLatitude - latitude) * 180 / Math.PI;
		speed = Math.sqrt(Math.pow(newLatitude - latitude, 2) + Math.pow(newLongitude - longitude, 2))*110*1000*60*60 / period;

		
		latitude = newLatitude;
		longitude = newLongitude;

		return this.toString();
	}
	@Override
	public String toString() {
		return "{'id':'" + id + "','timeStamp':" + System.currentTimeMillis() +
                ", 'lattitude': " + latitude + ",'longitude': " + longitude +
                ", 'speed' :"+ speed + ", 'angle':"+angle+", 'type' : 'VEHICLE'}";
	}
}
