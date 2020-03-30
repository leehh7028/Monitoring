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
		System.out.println("����͸� ����");
		mysql m = null;
		PushService ps = null;

		int beforeMembers = -1;
		
		int frequency = 1; // ������ �ֱ�
		int maximum = 20; // 1�е��� ������ �ִ� ���� �ȿ� �����ؾ� Ƚ��
		int count = 15; // 1�е��� ž�� ���� �ȿ� �����ؾ� Ƚ��
		double limitAccuracy_on = 2.5; // ž�� ���� (������ Accuracy��)
		double limitAccuracy_off = 4.0; // 
		
		double limitTemperature = 30.0; // ���� �µ�
		
		SensorMonitoring sm = new SensorMonitoring(limitTemperature); // ���� �� ��ĵ
		sm.start();// ���� �� ��ĵ ����
		
		while(true) {
			m = new mysql();
			
			ArrayList<DeviceInfo> macAddreass_list = m.select_device_info();
			System.out.println("�̿����� ȸ�� �� : " + macAddreass_list.size());
			
		    ArrayList<Thread> threads = new ArrayList<Thread>();
		    for(int i=0; i<macAddreass_list.size(); i++) {
//				if(beforeMembers < i) {
					ps = new PushService();
					System.out.println("ps ����");
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
		System.out.println("����͸� ����");
		mysql m = null;
		PushService ps = null;
		
		int index = 3; // Ȯ���� ������ ���ؽ�
		int frequency = 0; // ������ �ֱ�
		int maximum = 20; // 1�е��� ������ �ִ� ���� �ȿ� �����ؾ� Ƚ��
		int count = 15; // 1�е��� ž�� ���� �ȿ� �����ؾ� Ƚ��
		double limitAccuracy_on = 2.5; // ž�� ���� (������ Accuracy��)
		double limitAccuracy_off = 4.0; // 
		
		double limitTemperature = 30.0; // ���� �µ�
		
		m = new mysql();
		ps = new PushService();
		
		SensorMonitoring sm = new SensorMonitoring(limitTemperature); // ���� �� ��ĵ
		sm.start();// ���� �� ��ĵ ����
		
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
