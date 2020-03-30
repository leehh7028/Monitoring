package DB_Monitoring_Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DB_Monitoring_Service.Custom_Data_Type.DeviceVariable;

public class Monitoring {
	private ArrayList<DeviceVariable> list; // DB ����� accuracy, time ���� ����� list
	private int hour, min, now_hour, now_min, limit_hour, limit_min; // DB�� ����� �ð�, ���� �ð�, ���� �ð� - 60
	
	private int count; // 1�е��� ž�� ���� �ȿ� �����ؾ� Ƚ�� 
	private double limitAccuracy_on; // ž�� ���� (������ Accuracy��)
	private double limitAccuracy_off;
	private boolean flag;
	
	private String macAddress;
	
	private String today, today_before60; // ���� ��¥
	private Pattern datePattern, timePattern; // ���Խ� ����
	private Matcher dateMacher, timeMacher; // ���Խ� ���� ����
	private String patten_today, patten_date, patten_time; // ��¥, �ð��� ���� ���Խ� ���� ����
	
	private String busTime = "";

	private DeviceVariable dv = null;
	
	public Monitoring() {}
	
	public Monitoring(DeviceVariable dv , int count, double limitAccuracy_on, double limitAccuracy_off, boolean flag, String macAddress) {
		this.macAddress = macAddress;
		init(dv, count, limitAccuracy_on, limitAccuracy_off, flag);
	}
	
	// �ʱ�ȭ
	private void init(DeviceVariable dv, int count, double limitAccuracy_on, double limitAccuracy_off, boolean flag) {
		hour = 0; min = 0; now_hour = 0; now_min  = 0; 
		this.limitAccuracy_on = limitAccuracy_on; this.count = count;
		this.limitAccuracy_off = limitAccuracy_off; this.flag = flag;
		this.dv = dv;
		//���� ��¥ �ð� ����
		Date d = new Date();
		Date d2 = new Date(d.getTime() - (60 * 1000));
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		today = date.format(d);
		today_before60 = date.format(d2) + " " + time.format(d2);
		
		// ���Խ� ����
		patten_today = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})";
		patten_date = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		patten_time = "([0-9]{2}):([0-9]{2}):([0-9]{2})";
		timePattern = Pattern.compile(patten_time);
		datePattern = Pattern.compile(patten_date);
		
		// ���� �ð����� �ð��� ���� �и� ����
		timeMacher = timePattern.matcher(time.format(d));
		if(timeMacher.find()){
			now_hour = Integer.parseInt(timeMacher.group(1));	
			now_min = Integer.parseInt(timeMacher.group(2));
		}else {
			System.err.println("ERROR : ���� �ð� ����");
		}
		
		timeMacher = timePattern.matcher(time.format(d2));
		if(timeMacher.find()){
			limit_hour = Integer.parseInt(timeMacher.group(1));	
			limit_min = Integer.parseInt(timeMacher.group(2));
		}else {
			System.err.println("ERROR : ���� �ð� - 1�� ����");
		}
		
	}
	
	// 1�е��� ��� Accuracy���
	private double avgAccuracy() {
		double avg = 0.0, accuracy = 0.0;
		String date = "";
		ArrayList<Double> accuracyList = new ArrayList<>();
		// 1�е��� �ִ���� �ȿ� "maximum" �� �����ϸ�
		// ž�¹����� "count" �� �����ϴ��� Ȯ�� ��
		// ���� �Ѵٸ� �д� ��� Accuracy�� ���
		for(int i = 0; i < list.size(); i++) {
			System.out.println((i+1)+"��° - " + list.get(i).getTime() + "  " + list.get(i).getAccuracy());
			
			
			// DB�� �����  accuracy�� ���
			accuracy = list.get(i).getAccuracy();
			
			// DB�� ����� time�� ��¥, �ð��� ������ �и�
			dateMacher = datePattern.matcher(list.get(i).getTime());
			if(dateMacher.find()){
				date = dateMacher.group();
			}else {
				System.err.println("ERROR : DB�� ����� ��¥ ����");
			}
			
			timeMacher = timePattern.matcher(list.get(i).getTime());
			if(timeMacher.find()){		
				hour = Integer.parseInt(timeMacher.group(1));
				min = Integer.parseInt(timeMacher.group(2));
			}else {
				System.err.println("ERROR : DB�� ����� �ð� ����");
			}
			
			// ž�¹����� "count" �� �����ϴ��� Ȯ��
			if(today.equals(date) && (limit_hour <= hour && now_hour >= hour) && (limit_min <= min && now_min >= min)) {
				accuracyList.add(accuracy);
//				System.out.println("-- 60�� �̳��� ���� --");
			}else {
//				System.out.println("-- 60�� �ʰ� --"); 
			}
		}
		// ž�� ������ "count"�� �����ϸ� �д� ��� Accuracy�� ���
		System.err.println("����ID : " + macAddress + " ���� Ƚ�� : " + count + " ���� Ƚ��: " + accuracyList.size());
		if(accuracyList.size() >= count) {
			for(int i = 0; i <accuracyList.size(); i++)
				avg += accuracyList.get(i);
			avg /= accuracyList.size();
//			System.out.println("-- �д� ��� Accuracy : " + avg + " --");
		}else {
			avg = 999999999999.0; // ���ٸ� �ִ밪 �Է�
		}
		
		return avg;
	}
	
	// ��ǿ��� ���
	public boolean isRidding() {
		double avg = avgAccuracy();
		System.out.println("����ID : " + macAddress + " ��� : " + avg);
		
		if(flag && avg <= limitAccuracy_on) {
			dv.setTime(list.get(0).getTime()); // ž�½ð� ����
			return true;
		} else if(!flag && avg < limitAccuracy_off) {
			dv.setTime(list.get(0).getTime()); // ž�½ð� ����
			return true;
		} else {
			dv.setTime(list.get(0).getTime()); // �����ð� ����
			return false;
		}
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void setList(ArrayList<DeviceVariable> list) {
		this.list = list;
	}
	
	public void setBusTime(String busTime) {
		this.busTime = busTime;
	}
	
	public String getBusTime() {		
		return this.busTime;
	}
}
