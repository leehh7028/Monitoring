package DB_Monitoring_Service;

import java.util.ArrayList;

public class SensorMonitoring extends Thread{
	private PushService ps;
	private double limitTemperature;
	
	public SensorMonitoring(double limitTemperature) {
		ps = new PushService();
		this.limitTemperature = limitTemperature;
	}
	public void run() {
		mysql m = new mysql();
//		ArrayList<String> tokens = m.select_Tokens();
		double temperature = 0.0;
	
		while(true) {
			temperature = m.select_recent_temperature();
			if(limitTemperature >= temperature && ps.getIsSendPush()) {
				ps.sendPushMessage(1, "/topics/Alarm", "차안의 온도가 높습니다", "현재 온도 : " + String.valueOf(temperature));
				ps.setIsSendPush(false);
			}else if(limitTemperature < temperature) {
				ps.setIsSendPush(true);
				
			}
		}
	}

}
