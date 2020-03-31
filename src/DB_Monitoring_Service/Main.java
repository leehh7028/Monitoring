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
