package DB_Monitoring_Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DB_Monitoring_Service.Custom_Data_Type.DeviceInfo;


public class Main{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		SingleThreadMode();
		MutiThreadMode();	
	}
	
	
	private static void MutiThreadMode() {
		System.out.println("모니터링 시작");
		mysql m = null;
		PushService ps = null;

		int beforeMembers = -1;
		
		int frequency = 1; // 쓰레드 주기
		int maximum = 20; // 1분동안 설정안 최대 범위 안에 존재해야 횟수
		int count = 15; // 1분동안 탑승 범위 안에 존재해야 횟수
		double limitAccuracy_on = 2.5; // 탑승 범위 (비콘의 Accuracy값)
		double limitAccuracy_off = 4.0; // 
		
		double limitTemperature = 30.0; // 설정 온도
		
		SensorMonitoring sm = new SensorMonitoring(limitTemperature); // 센서 값 스캔
		sm.start();// 센서 값 스캔 시작
		
		while(true) {
			m = new mysql();
			
			ArrayList<DeviceInfo> macAddreass_list = m.select_device_info();
			System.out.println("이용중인 회원 수 : " + macAddreass_list.size());
			
		    ArrayList<Thread> threads = new ArrayList<Thread>();
		    for(int i=0; i<macAddreass_list.size(); i++) {
//				if(beforeMembers < i) {
					ps = new PushService();
					System.out.println("ps 생성");
//				}
		    	MonitoringThread mt = new MonitoringThread(ps, macAddreass_list.get(i).getUserID(),frequency, maximum,macAddreass_list.get(i).getMacAddress() ,count, limitAccuracy_on, limitAccuracy_off);
		        mt.start();
		        threads.add(mt);   
		    }
		    
			beforeMembers = macAddreass_list.size()-1;
			
		    for(int i=0; i<threads.size(); i++) {
		        Thread t = threads.get(i);
		        try {
		            t.join();
		        }catch(Exception e) {
		        }
		    }
		}
		
	}
	
	private static void SingleThreadMode() {
		System.out.println("모니터링 시작");
		mysql m = null;
		PushService ps = null;
		
		int index = 3; // 확인할 비콘의 인텍스
		int frequency = 0; // 쓰레드 주기
		int maximum = 20; // 1분동안 설정안 최대 범위 안에 존재해야 횟수
		int count = 15; // 1분동안 탑승 범위 안에 존재해야 횟수
		double limitAccuracy_on = 2.5; // 탑승 범위 (비콘의 Accuracy값)
		double limitAccuracy_off = 4.0; // 
		
		double limitTemperature = 30.0; // 설정 온도
		
		m = new mysql();
		ps = new PushService();
		
		SensorMonitoring sm = new SensorMonitoring(limitTemperature); // 센서 값 스캔
		sm.start();// 센서 값 스캔 시작
		
		ArrayList<DeviceInfo> macAddreass_list = m.select_device_info();
		
	    ArrayList<Thread> threads = new ArrayList<Thread>();
	    
	    System.out.println(macAddreass_list.get(index).getUserID() + " " + macAddreass_list.get(index).getMacAddress());
	    
	    MonitoringThread mt = new MonitoringThread(ps, macAddreass_list.get(index).getUserID(),frequency, maximum, macAddreass_list.get(index).getMacAddress() ,count, limitAccuracy_on, limitAccuracy_off);
	    mt.start();
	    threads.add(mt);

	    for(int i=0; i<threads.size(); i++) {
	        Thread t = threads.get(i);
	        try {
	            t.join();
	        }catch(Exception e) {
	        }
	    }

	}
	

}
