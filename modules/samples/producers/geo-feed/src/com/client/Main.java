package com.client;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Will create a single thread for each file. add all input file names and corresponding End Points and the delay between messages to the fileList array
        String urlAW = "http://localhost:9763/endpoints/GpsDataOverHttpSpatialObjectStream/";
//	String urlAW = "http://wso2.knnect.com:9763/endpoints/GPSStreamAdaptor/trackingstream"; //AWS
 	// String urlAW = "http://10.100.5.110:9763/endpoints/GPSStreamAdaptor/trackingstream";
      String delay1 = "1000";
      String[][] fileList  = {{"set01.txt",urlAW, "500"},{"set02.txt",urlAW, "400"},{"set03.txt",urlAW,"250"},{"set04.txt",urlAW,"500"},{"set05.txt",urlAW,"1000"},{"set07.txt",urlAW,"600"},{"set08.txt",urlAW,"1000"},{"set10.txt",urlAW,"500"},{"set11.txt",urlAW,"500"},{"set12.txt",urlAW,"500"}};
     // String[][] fileList  = {{"set01.txt",urlAW, "500"},{"set03.txt",urlAW,"250"},{"set04.txt",urlAW,"500"}};
       // String[][] fileList  = {{"set03.txt",urlAW,"250"},{"set04.txt",urlAW,"500"}};

        //Saving Thread List
        List<Thread> threads = new ArrayList<Thread>();

      for (int i = fileList.length; i >0; i--) {
            // parse the URI and the text file name
            Runnable builder = new Runner(fileList[i-1][0],fileList[i-1][1],Integer.parseInt(fileList[i-1][2]));

            Thread worker = new Thread(builder);

          worker.setName(String.valueOf(i));
          //starting the thread
            worker.start();

         // Remembering the thread for later usage
          threads.add(worker);

        }
    }
}
