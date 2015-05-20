package org.wso2.carbon.sample.tfl;

import org.wso2.carbon.sample.tfl.Bus.Bus;

import java.util.ArrayList;
import java.util.Collection;

public class Update extends Thread {
	public long currentTime;
	public long prevTime;
	public long period;
	public String endPoint;

	public Update(long time, long period, String endPoint) {
		super();
		currentTime = time;
		prevTime = time;
		this.period = period;
		this.endPoint = endPoint;
	}

	public void run() {
		while (true) {
			try {
				if (TflStream.lastTime != 0) {
					//System.out.println("Updating");
					Collection<Bus> busses = TflStream.busses.values();
					ArrayList<String> jsonList = new ArrayList<String>();
					for (Bus bus : busses) {
						String msg = bus.move(currentTime, period);
						if (msg != null)
							jsonList.add(msg);
					}
					currentTime += period;
					TflStream.send(jsonList, endPoint);

					long a = currentTime - System.currentTimeMillis();
					//System.out.println("a " + a);
					if (a >= 0) {
						Thread.sleep(a);
					} else {
						currentTime = System.currentTimeMillis();
					}

				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//throw new RuntimeException("asd");
			}
		}
	}
}