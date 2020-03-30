package DB_Monitoring_Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DB_Monitoring_Service.Custom_Data_Type.DeviceVariable;

public class Monitoring {
	private ArrayList<DeviceVariable> list; // DB 저장된 accuracy, time 값이 저장된 list
	private int hour, min, now_hour, now_min, limit_hour, limit_min; // DB에 저장된 시간, 현재 시간, 현재 시간 - 60
	
	private int count; // 1분동안 탑승 범위 안에 존재해야 횟수 
	private double limitAccuracy_on; // 탑승 범위 (비콘의 Accuracy값)
	private double limitAccuracy_off;
	private boolean flag;
	
	private String macAddress;
	
	private String today, today_before60; // 현재 날짜
	private Pattern datePattern, timePattern; // 정규식 패턴
	private Matcher dateMacher, timeMacher; // 정규식 패턴 적용
	private String patten_today, patten_date, patten_time; // 날짜, 시간에 맞춘 정규식 패턴 선언
	
	private String busTime = "";

	private DeviceVariable dv = null;
	
	public Monitoring() {}
	
	public Monitoring(DeviceVariable dv , int count, double limitAccuracy_on, double limitAccuracy_off, boolean flag, String macAddress) {
		this.macAddress = macAddress;
		init(dv, count, limitAccuracy_on, limitAccuracy_off, flag);
	}
	
	// 초기화
	private void init(DeviceVariable dv, int count, double limitAccuracy_on, double limitAccuracy_off, boolean flag) {
		hour = 0; min = 0; now_hour = 0; now_min  = 0; 
		this.limitAccuracy_on = limitAccuracy_on; this.count = count;
		this.limitAccuracy_off = limitAccuracy_off; this.flag = flag;
		this.dv = dv;
		//현재 날짜 시간 저장
		Date d = new Date();
		Date d2 = new Date(d.getTime() - (60 * 1000));
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		today = date.format(d);
		today_before60 = date.format(d2) + " " + time.format(d2);
		
		// 정규식 패턴
		patten_today = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})";
		patten_date = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		patten_time = "([0-9]{2}):([0-9]{2}):([0-9]{2})";
		timePattern = Pattern.compile(patten_time);
		datePattern = Pattern.compile(patten_date);
		
		// 현재 시간에서 시간과 분을 분리 저장
		timeMacher = timePattern.matcher(time.format(d));
		if(timeMacher.find()){
			now_hour = Integer.parseInt(timeMacher.group(1));	
			now_min = Integer.parseInt(timeMacher.group(2));
		}else {
			System.err.println("ERROR : 현재 시간 에러");
		}
		
		timeMacher = timePattern.matcher(time.format(d2));
		if(timeMacher.find()){
			limit_hour = Integer.parseInt(timeMacher.group(1));	
			limit_min = Integer.parseInt(timeMacher.group(2));
		}else {
			System.err.println("ERROR : 현재 시간 - 1분 에러");
		}
		
	}
	
	// 1분동안 평균 Accuracy계산
	private double avgAccuracy() {
		double avg = 0.0, accuracy = 0.0;
		String date = "";
		ArrayList<Double> accuracyList = new ArrayList<>();
		// 1분동안 최대범위 안에 "maximum" 번 존재하면
		// 탑승범위에 "count" 번 존재하는지 확인 후
		// 존재 한다면 분당 평균 Accuracy를 계산
		for(int i = 0; i < list.size(); i++) {
			System.out.println((i+1)+"번째 - " + list.get(i).getTime() + "  " + list.get(i).getAccuracy());
			
			
			// DB에 저장된  accuracy를 출력
			accuracy = list.get(i).getAccuracy();
			
			// DB에 저장된 time을 날짜, 시간과 분으로 분리
			dateMacher = datePattern.matcher(list.get(i).getTime());
			if(dateMacher.find()){
				date = dateMacher.group();
			}else {
				System.err.println("ERROR : DB에 저장된 날짜 에러");
			}
			
			timeMacher = timePattern.matcher(list.get(i).getTime());
			if(timeMacher.find()){		
				hour = Integer.parseInt(timeMacher.group(1));
				min = Integer.parseInt(timeMacher.group(2));
			}else {
				System.err.println("ERROR : DB에 저장된 시간 에러");
			}
			
			// 탑승범위에 "count" 번 존재하는지 확인
			if(today.equals(date) && (limit_hour <= hour && now_hour >= hour) && (limit_min <= min && now_min >= min)) {
				accuracyList.add(accuracy);
//				System.out.println("-- 60초 이내로 들어옴 --");
			}else {
//				System.out.println("-- 60초 초과 --"); 
			}
		}
		// 탑승 범위에 "count"번 존재하면 분당 평균 Accuracy를 계산
		System.err.println("비콘ID : " + macAddress + " 지정 횟수 : " + count + " 들어온 횟수: " + accuracyList.size());
		if(accuracyList.size() >= count) {
			for(int i = 0; i <accuracyList.size(); i++)
				avg += accuracyList.get(i);
			avg /= accuracyList.size();
//			System.out.println("-- 분당 평균 Accuracy : " + avg + " --");
		}else {
			avg = 999999999999.0; // 없다면 최대값 입력
		}
		
		return avg;
	}
	
	// 재실여부 계산
	public boolean isRidding() {
		double avg = avgAccuracy();
		System.out.println("비콘ID : " + macAddress + " 평균 : " + avg);
		
		if(flag && avg <= limitAccuracy_on) {
			dv.setTime(list.get(0).getTime()); // 탑승시간 저장
			return true;
		} else if(!flag && avg < limitAccuracy_off) {
			dv.setTime(list.get(0).getTime()); // 탑승시간 저장
			return true;
		} else {
			dv.setTime(list.get(0).getTime()); // 하차시간 저장
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
