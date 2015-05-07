package com.client;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by yasassri on 6/6/14.
 */
public class Runner implements Runnable {
    private final String fileName;
    private final String endPoint;
    private final int delay;

    Runner(String fileName, String endPoint, int delay) {
        this.fileName = fileName;
        this.endPoint = endPoint;
        this.delay = delay;
    }

    @Override
    public void run() {

      try {
            simulateMovingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    protected void simulateMovingVehicle() throws Exception {

        // The text file has ID, lon, lat, Time
        BufferedReader br;

        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {

                //use comma as separator
                String[] gps = line.split(cvsSplitBy);
                GPSData mydata = createGPSObject( gps);

                move( mydata);
                Thread.sleep(1000);
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("input output error : " +e.getMessage());
            e.printStackTrace();
        }

    }

    protected void move(GPSData data) {

        HttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(endPoint);
        try {
            Gson gson = new Gson();
            gson.toJson(data);

            StringEntity entity = new StringEntity(gson.toJson(data));
            post.setEntity(entity);
            System.out.println("The Message Sent : "+gson.toJson(data));
            HttpResponse response = client.execute(post);
            //System.out.println(response);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static GPSData createGPSObject(String [] packet){
        String id = packet[0];
        String time = packet[1];
        String latitude = packet[2];
        String longitude = packet[3];
        String elevation = packet[4];
        //   String accuracy = packet[5];
        //   String bearing = packet[6];
        String speed = packet[7];


        String idval = id;


        //2014-06-04T01:44:30Z timestamp creating
        // System.out.println(time);

        int year = Integer.parseInt(time.substring(0, 4));
        int month = Integer.parseInt(time.substring(5, 7));
        int date = Integer.parseInt(time.substring(8, 10));
        int hour =Integer.parseInt(time.substring(11,13));
        int minutes =Integer.parseInt(time.substring(14,16));
        int seconds =Integer.parseInt(time.substring(17,19));


        long lngtime = (long)(((hour* 60*60*1000) + minutes* 60 *1000) + seconds * 1000);
        long timeinmillis = returnSeconds(year,month,date);


        long finaltime = lngtime+ timeinmillis;

        double timefinal = (double)finaltime;


        GPSData data = new GPSData();
        data.setId(idval);
        //data.setTimeStamp(timefinal);
        data.setTimeStamp(System.currentTimeMillis());
        data.setType("VEHICLE");
        // data.setTimeStamp(Double.parseDouble(time));
        data.setLattitude(Double.parseDouble(latitude));
        data.setLongitude(Double.parseDouble(longitude));
        data.setSpeed(Double.parseDouble(speed));
        data.setAngle(Double.parseDouble(elevation));

        return data;
    }

    //getting a reference time for time

    public static long returnSeconds(int year, int month, int date) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();

        calendar1.set(1970, 01, 01);
        calendar2.set(year, month, date);

        long milliseconds1 = calendar1.getTimeInMillis();
        long milliseconds2 = calendar2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long seconds = diff;


        //System.out.println(seconds);
        return seconds;
    }


}
