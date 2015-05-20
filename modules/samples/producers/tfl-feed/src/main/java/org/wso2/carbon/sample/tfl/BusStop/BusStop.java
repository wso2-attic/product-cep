package org.wso2.carbon.sample.tfl.BusStop;

public class BusStop {
	public String id;
	public double longitude;
	public double latitude;
	
	public BusStop(String StopID, double lat, double lon) {
		this.id = StopID;
		this.latitude = lat;
		this.longitude = lon;
	}

	@Override
	public String toString() {
		return "{'id':'" + id + "','timeStamp':" + System.currentTimeMillis() +
                ", 'lattitude': " + latitude + ",'longitude': " + longitude +
                ", 'speed' :"+ 0 + ", 'angle':"+0+", 'type' : 'STOP'}";
	}
}
