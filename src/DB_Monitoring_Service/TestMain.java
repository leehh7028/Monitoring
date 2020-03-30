package DB_Monitoring_Service;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DB_Monitoring_Service.Custom_Data_Type.DeviceVariable;

public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		CheckRidding();
//		timer60();
//		PushService ps = new PushService();
//		ps.sendPushMessage("test1", "자바에서 온 푸시메시지 3 제목", "자바에서 온 푸시메시지 3");
//		System.out.println("End");
//		fortest();
		aadf();
//		TestData();
	}

	
	public static void TestData() {
		mysql m = new mysql();
		ArrayList<DeviceVariable> a = m.Test_interference();
		String name = "Test2";
        BufferedWriter bufferedWriter = null;
		  try{
           //파일 객체 생성
	        bufferedWriter = Files.newBufferedWriter(Paths.get("C:\\Users\\Junhyeong\\Desktop\\data\\"+name+".csv"),Charset.forName("UTF-8"));
	        bufferedWriter.write("Macaddress");
            bufferedWriter.write(",");
            bufferedWriter.write("Rssi");
            bufferedWriter.write(",");
            bufferedWriter.write("Txpower");
            bufferedWriter.write(",");
            bufferedWriter.write("Accuracy");
            bufferedWriter.write(",");
            bufferedWriter.write("Time");
            bufferedWriter.newLine();
            
        	for(int i = 0; i < a.size(); i++) {
	            bufferedWriter.write(a.get(i).getMacaddress());
	            bufferedWriter.write(",");
	            bufferedWriter.write(String.valueOf(a.get(i).getRssi()));
	            bufferedWriter.write(",");
	            bufferedWriter.write(String.valueOf(a.get(i).getTxpower()));
	            bufferedWriter.write(",");
	            bufferedWriter.write(String.valueOf(a.get(i).getAccuracy()));
	            bufferedWriter.write(",");
	            bufferedWriter.write(a.get(i).getTime());
	            
	            bufferedWriter.newLine();
        	}
 
                
                bufferedWriter.close();



		  }catch(FileNotFoundException e){
	            e.printStackTrace();
	        }catch(IOException e){
	            e.printStackTrace();
	        }finally{
	            try{
	                if(bufferedWriter != null){
	                	bufferedWriter.close();
	                }
	            }catch(IOException e){
	                e.printStackTrace();
	            }
	        }

		
	}
	public static void fortest() {
		int max = 15;
        int even = max % 2;
//        System.out.println(even);
        
        for(int i = 0; i < max; i+=2) {
        	for(int j = i; j < i+2; j++) {
        		if(even == 1 && max != j) {
        		System.out.println(j);
        		}else if(even == 0) {
        			System.out.println(j);
        		}     	
        	}
        }
        
	}
	
	
	public static void aadf() {
		String aa ="{\"message\":\"아이가 버스에 탑승했습니다. 탑승 시간 : 2019-02-12 00:02:35\"}";
		String bb = "{\"message\":\"현재 온도 : 30.66\"}";
		String patten_message = "\\{\"([a-zA-Z]{7})\":\"(.+)\"\\}";
		
		
		String g1 = "";
		String g2 = "";

		
		Pattern pattern = Pattern.compile(patten_message);
		Matcher macher;
		
		macher = pattern.matcher(bb);
		if(macher.find()){
			g2 =  String.valueOf(macher.group(2));
		}
		
		System.out.println(g1);
		System.out.println(g2);
        
	}
	
	
	
	public static void CheckRidding() {
		int min = 0, sec = 0, now_min  = 0, now_sec  = 0;
		double accuracy = 0.0;
		int count = 5;
		double avg = 0.0;
		ArrayList<Double> accuracyList = null;
		Date d = new Date();
		
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");

//		String today = date.format(d) + " " + time.format(d);
//		System.out.println(today);
		
		String today = "2019-01-23 17:27:54";
		
		String patten_today = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})";
		String patten_date = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		String patten_time = "([0-9]{2}):([0-9]{2}):([0-9]{2})";
		
		Pattern timePattern = Pattern.compile(patten_time), datePattern = Pattern.compile(patten_date);
		Matcher timeMacher, dateMacher;
		
		timeMacher = timePattern.matcher(today);
		if(timeMacher.find()){
			now_min = Integer.parseInt(timeMacher.group(2));
			now_sec = Integer.parseInt(timeMacher.group(3));		
		}
		
		mysql m = new mysql();
		ArrayList<DeviceVariable> recent_list = m.select_recent(20, "b8:27:eb:1c:46:ee");
		
		accuracyList = new ArrayList<>();
		for(int i = 0; i < recent_list.size(); i++) {
			System.out.println((i+1)+"번째");
			System.out.println(recent_list.get(i).getTime() + "  " + recent_list.get(i).getAccuracy());
			accuracy = recent_list.get(i).getAccuracy();
			
			dateMacher = datePattern.matcher(recent_list.get(i).getTime());
			if(dateMacher.find()){
				today = dateMacher.group();
			}
					
			timeMacher = timePattern.matcher(recent_list.get(i).getTime());
			if(timeMacher.find()){		
				min = Integer.parseInt(timeMacher.group(2));
				sec = Integer.parseInt(timeMacher.group(3));
			}
			
			if(now_min == min) {
				accuracyList.add(accuracy);
			}
		}
		if(accuracyList.size() >= count) {
			for(int i = 0; i <accuracyList.size(); i++)
				avg += accuracyList.get(i);
			avg /= accuracyList.size();
		}
		
		System.out.println("=====================");
		for(int i = 0; i <accuracyList.size(); i++)
			System.out.println(accuracyList.get(i));
			
		System.out.println(avg);
	}
	
	public static void RealtimeTest() {

		Date d = new Date();
		
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
		String today = date.format(d) + " " + time.format(d);
		System.out.println(today);
		
		String value = "[2018-01-01] [ERROR] [Nesoy Log Time : 50] [" + today + "]";
		System.out.println(value);
		
		String patten_today = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})";
		String patten_date = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		String patten_time = "([0-9]{2}):([0-9]{2}):([0-9]{2})";
		
		Pattern todayPattern = Pattern.compile(patten_today);
		Matcher todayMacher = todayPattern.matcher(value);
		if(todayMacher.find()){
			System.out.println(todayMacher.group());
		}
		
		Pattern datePattern = Pattern.compile(patten_date);
		Matcher dateMacher = datePattern.matcher(value);
		if(dateMacher.find()){
			System.out.println(dateMacher.group());
		}
		
		Pattern timePattern = Pattern.compile(patten_time);
		Matcher timeMacher = timePattern.matcher(value);
		if(timeMacher.find()){
			System.out.println(timeMacher.group());
			System.out.println("a " + timeMacher.group(0));
			System.out.println("b " + timeMacher.group(1));
			System.out.println("c " + timeMacher.group(2));
			System.out.println("d " + timeMacher.group(3));
			
		}
		
		
		mysql m = new mysql();
//		ArrayList<DeviceVariable> device_variable_list = m.select_device_variable_info_tb();
		ArrayList<DeviceVariable> recent_list = m.select_recent(20, "b8:27:eb:1c:46:ee");
		
		for(int i = 0; i < recent_list.size(); i++) {
			System.out.println((i+1)+"번째");
			System.out.println(recent_list.get(i).getTime() + "  " + recent_list.get(i).getAccuracy());
			
			datePattern = Pattern.compile(patten_date);
			dateMacher = datePattern.matcher(recent_list.get(i).getTime());
			if(dateMacher.find()){
				System.out.println(dateMacher.group());
			}
			
			timePattern = Pattern.compile(patten_time);
			timeMacher = timePattern.matcher(recent_list.get(i).getTime());
			if(timeMacher.find()){
				System.out.println(timeMacher.group());
				System.out.println("a " + timeMacher.group(0));
				System.out.println("b " + timeMacher.group(1));
				System.out.println("c " + timeMacher.group(2));
				System.out.println("d " + timeMacher.group(3));
				
			}
			
		}
	}
	
	public static void timer60() {
		Date d = new Date();
		Date d2 = new Date(d.getTime() - (60 * 1000));
		
		
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");
		String today = date.format(d) + " " + time.format(d);
		System.out.println("현재 시간 : " + today);
		
		String today_60 = date.format(d2) + " " + time.format(d2);
		System.out.println("현재 시간 : " + today_60);
		
	

		String patten_today = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})";
		String patten_date = "([0-9]{4})-([0-9]{2})-([0-9]{2})";
		String patten_time = "([0-9]{2}):([0-9]{2}):([0-9]{2})";
		
		Pattern todayPattern = Pattern.compile(patten_today);
		Matcher todayMacher = todayPattern.matcher(today);
		if(todayMacher.find()){
			System.out.println(todayMacher.group());
		}
		
		Pattern datePattern = Pattern.compile(patten_date);
		Matcher dateMacher = datePattern.matcher(today);
		if(dateMacher.find()){
			System.out.println(dateMacher.group());
		}
		
		Pattern timePattern = Pattern.compile(patten_time);
		Matcher timeMacher = timePattern.matcher(today);
		if(timeMacher.find()){
			System.out.println(timeMacher.group());
			System.out.println("a " + timeMacher.group(0));
			System.out.println("b " + timeMacher.group(1));
			System.out.println("c " + timeMacher.group(2));
			System.out.println("d " + timeMacher.group(3));
			
		}
		
		
		
	}

}
