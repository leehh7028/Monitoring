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
				DeviceVariable dv = new DeviceVariable(); // ������ �ð��� �����ϱ� ����
				ArrayList<DeviceVariable> recent_list = m.select_recent(maximum, macAddress);
				if(recent_list.size() > 0) {
					Monitoring monitor = new Monitoring(dv, count, limitAccuracy_on, limitAccuracy_off, ps.getIsSendPush(), macAddress); // 
					monitor.setList(recent_list);
					boolean isRidding = monitor.isRidding();
					System.out.println("����ID : " + macAddress + " ��� ���� : " + isRidding + " ž�� ���� / Ǫ�� ���� : " + isRidding + " / " + ps.getIsSendPush());
					
					// ������� ���� �޽����� �׻� ���� ��
					if(isRidding && ps.getIsSendPush()) {
						// Ǫ�� ����
						ps.sendPushMessage(0,ID,"���̰� ������ ž���߽��ϴ�.", "ž�� �ð� : " + dv.getTime());
						// db ���� - ž�½ð�, ���� id, ž�¿���
						m.input_timetable(ID, dv.getTime(), 0);
						ps.setIsSendPush(false);
					}else if(!isRidding && !ps.getIsSendPush()){
						// Ǫ�� ����
						ps.sendPushMessage(0,ID,"���̰� ������ �����߽��ϴ�.", "���� �ð� : " + dv.getTime());
						// db ���� - ž�½ð�, ���� id, ž�¿���
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
