package DB_Monitoring_Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import DB_Monitoring_Service.Custom_Data_Type.DeviceInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;



public class Main{

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	
		
		// Use a service account
		InputStream serviceAccount = new FileInputStream("service-account-file.json");
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		FirebaseOptions FBoptions = new FirebaseOptions.Builder()
		    .setCredentials(credentials)
		    .build();
		FirebaseApp.initializeApp(FBoptions);

		Firestore db = FirestoreClient.getFirestore();


		// Create a new user with a first and last name
		Map<String, Object> user = new HashMap<>();
		user.put("first", "Ada");
		user.put("last", "Lovelace");
		user.put("born", 1815);

		/*
		DocumentReference docRef = db.collection("users").document("alovelace");
		// Add document data  with id "alovelace" using a hashmap
		Map<String, Object> data = new HashMap<>();
		data.put("first", "Ada");
		data.put("last", "Lovelace");
		data.put("born", 1815);
		//asynchronously write data
		ApiFuture<WriteResult> result = docRef.set(data);
		// ...
		// result.get() blocks on response
		try {
			System.out.println("Update time : " + result.get().getUpdateTime());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		DocumentReference docRef = db.collection("users").document("aturing");
		// Add document data with an additional field ("middle")
		Map<String, Object> data = new HashMap<>();
		data.put("first", "Alan");
		data.put("middle", "Mathison");
		data.put("last", "Turing");
		data.put("born", 1912);

		ApiFuture<WriteResult> result = docRef.set(data);
		try {
			System.out.println("Update time : " + result.get().getUpdateTime());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.print("test");
		
//		SingleThreadMode();
		// MutiThreadMode();	
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
