
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class Main
{

    public static void main(String[] args) throws IOException
    {
        // TODO Auto-generated method stub
        MutiThreadMode();
        
    	//FirebaseStorage fbs = new FirebaseStorage();
        //fbs.FirebaseConnet();
        //fbs.isRidding(false, "40001", "30530");
        //fbs.Log();
        //fbs.busSensorDataModify();
    }

    private static void MutiThreadMode()
    {

        System.out.println("모니터링 시작");
        mysql m = null;
        PushService ps = null;

        int beforeMembers = -1;

        int frequency = 1; // 쓰레드 주기
        int maximum = 20; // 1분동안 설정안 최대 범위 안에 존재해야 횟수
        int count = 15; // 1분동안 탑승 범위 안에 존재해야 횟수
        double limitAccuracy_on = 2.5;  // 탑승 범위 (비콘의 Accuracy값)
        double limitAccuracy_off = 4.0; //

        double limitTemperature = 30.0; // 설정 온도


        // 센서 값 스캔
        SensorMonitoring sm = new SensorMonitoring(limitTemperature);
        sm.start();



    }
}
