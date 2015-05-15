package com.client;

/**
 * Created by yasassri on 6/6/14.
 */
//id, time,lat,lon,elevation,accuracy,bearing,speed
public class GPSData {
    private String id;
    private long timeStamp;
    private double lattitude;
    private double longitude;
    private double speed;
    private double angle;
    private String type;

    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getType(){ return type;}
    public void setType(String type){ this.type = type;}
    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public double getLattitude() {
        return lattitude;
    }
    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

}
