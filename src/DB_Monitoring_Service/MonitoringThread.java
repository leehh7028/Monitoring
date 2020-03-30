package DB_Monitoring_Service;

import java.util.ArrayList;

import DB_Monitoring_Service.Custom_Data_Type.DeviceInfo;
import DB_Monitoring_Service.Custom_Data_Type.DeviceVariable;

public class MonitoringThread extends Thread{
	private String ID;
	private int frequency;
	private int maximum;
	private String macAddress;
	private int count;
	private double limitAccuracy_on;
	private double limitAccuracy_off;
	private boolean flag;
	
	private mysql m;
	private PushService ps;

	public MonitoringThread(PushService ps, String ID, int frequency, int maximum, String macAddress, int count, double limitAccuracy_on, double limitAccuracy_off) {
		this.ps = ps;
		this.ID = ID;
		this.frequency = frequency;
		this.maximum = maximum;
		this.macAddress = macAddress;
		this.count = count;
		this.limitAccuracy_on = limitAccuracy_on;
		this.limitAccuracy_off = limitAccuracy_off;
	}
	
	public void run() {
		try {
			while(true) {
				m = new mysql();
				
				Monitoring mt = new Monitoring();
				DeviceVariable dv = new DeviceVariable(); // 승하차 시간을 저장하기 위해
				ArrayList<DeviceVariable> recent_list = m.select_recent(maximum, macAddress);
				if(recent_list.size() > 0) {
					Monitoring monitor = new Monitoring(dv, count, limitAccuracy_on, limitAccuracy_off, ps.getIsSendPush(), macAddress); // 
					monitor.setList(recent_list);
					boolean isRidding = monitor.isRidding();
					System.out.println("비콘ID : " + macAddress + " 재실 여부 : " + isRidding + " 탑승 여부 / 푸시 여부 : " + isRidding + " / " + ps.getIsSendPush());
					
					// 스레드로 인해 메시지가 항상 가게 됨
					if(isRidding && ps.getIsSendPush()) {
						// 푸시 전송
						ps.sendPushMessage(0,ID,"아이가 버스에 탑승했습니다.", "탑승 시간 : " + dv.getTime());
						// db 저장 - 탑승시간, 유저 id, 탑승여부
						m.input_timetable(ID, dv.getTime(), 0);
						ps.setIsSendPush(false);
					}else if(!isRidding && !ps.getIsSendPush()){
						// 푸시 전송
						ps.sendPushMessage(0,ID,"아이가 버스에 하차했습니다.", "하차 시간 : " + dv.getTime());
						// db 저장 - 탑승시간, 유저 id, 탑승여부
						m.input_timetable(ID, dv.getTime(), 1);
						ps.setIsSendPush(true);
					}	
				}
				sleep(frequency * 1000);
			}
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
