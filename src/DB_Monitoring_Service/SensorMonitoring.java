package DB_Monitoring_Service;

import java.util.ArrayList;
import java.util.List;
import DB_Monitoring_Service.Custom_Data_Type.DeviceInfo;


/*
 * 2020-04-05 수정 App 변경으로 인해 더이상의 push service가 필요 없어졌음. Push service 기능들은 모두 off 해야 함 센서 모니터링에서는 현재
 * 센서가 감지한 온도 조도 습도를 Firebase로 전송하기만 하면 됨
 * 
 */
public class SensorMonitoring extends Thread
{
    // private PushService ps;
    private double limitTemperature;
    private int count = 0;
    private int accuracyCount = 0;
    List<DeviceInfo> deviceInfo = new ArrayList<DeviceInfo>();
    private boolean duplication = false;
    double avgAccuracy = 0.0;
    

    public SensorMonitoring(double limitTemperature)
    {
        // ps = new PushService();
        this.limitTemperature = limitTemperature;
    }


    public void run()
    {
        mysql m = new mysql();
        Double temperature = 0.0;
        Double humidity = 0.0;
        Double illumination = 0.0;

        while (true)
        {
            temperature = m.select_recent_temperature();    // 센서 정보 저장
            humidity = m.select_recent_humidity();          //
            illumination = m.select_recent_illumination();  //

            // 센서 정보 출력
            System.out.println("----------------\t 센서 정보 출력 \t----------------");
            System.out.print("Temperature : " + temperature + ", ");
            System.out.print("humidity : " + humidity + ", ");
            System.out.println("illumination : " + illumination + ", ");
            System.out.println();

            // 디바이스 정보
            // [0] MAC, [1] RSSI, [2] TX, [3] Accuracy, time
            System.out.println("----------------\t DB 디바이스 정보 출력 \t----------------");
            String[] strDevice = m.device_variable_info_tb().split(" ");        // 읽어온 DB 정보를 분할해서 저장    
            for (int i = 0; i < strDevice.length; i++)
            {
                System.out.print(strDevice[i] + " ");       // 읽어들인 정보를 하나씩 출력
            }            
            System.out.println();
            
            // UUID major minor 출력
            // [0] UUID, [1] major, [2] minor
            System.out.println("UUID major minor: " + m.select_device_uuid(strDevice[0]));
            String[] uniqueDevice = m.select_device_uuid(strDevice[0]).split(" ");          
            System.out.println();
            
            // 첫번째로 돌아가는 경우 리스트에 아무것도 없기 때문에 바로 리스트에 추가
            if (count == 0)
            {
                DeviceInfo first = new DeviceInfo();        // 디바이스 객체 클래스 생성
                first.setMacAddress(strDevice[0]);          // MAC 주소 추가
                first.setAccuracy(strDevice[3]);            // Accuracy 값 추가
                first.setUUID(uniqueDevice[0]);             // UUID
                first.setMajor(uniqueDevice[1]);            // Major
                first.setMinor(uniqueDevice[2]);            // Minor                
                deviceInfo.add(first);                      // 해당 객체를 객체 리스트에 추가
                count++;
            }
            
            /*
             * 리스트로 디바이스 정보 수집
             * 객체 리스트를 사용하여 디바이스가 추가 될때마다 동적 할당 
             * DB에서 가장 마지막에 읽어온 값에서 MAC을 기반으로 비교 및 추가
             * 만일 동일한 MAC 데이터가 읽어졌으면 Accuracy 값만을 추가
             * MAC 주소가 다를 경우 리스트에 새로운 디바이스 생성 후 추가 
             */          
            System.out.println("----------------\t MAC Compare \t----------------");
            for(int i = 0 ; i < deviceInfo.size(); i++)
            {   
                DeviceInfo DevinfoMacCompare = deviceInfo.get(i);                           // Devinfo에 저장된 객체 순서대로 호출
                System.out.println("Device MAC \t: " + DevinfoMacCompare.getMacAddress());    
                System.out.println("Serch Dev MAC \t: " + strDevice[0]);
                // MAC 주소가 같을 경우
                if(DevinfoMacCompare.getMacAddress().equals(strDevice[0]))                  // 호출된 DeviceInfo 클래스의 MAC 파라미터 비교
                {
                    System.out.println("MAC 주소가 같음");
                    DevinfoMacCompare.setAccuracy(strDevice[3]);    // 해당 디바이스 객체 클래스에 Accuracy 값 추가
                    duplication = true;                             // 해당 MAC 정보가 이미 중복되어 있다는 것을 알리기 위한 flag 값
                    System.out.println("Accuary 값 추가  : " + DevinfoMacCompare.getNumAccuracy(DevinfoMacCompare.getAccuracy().size()-1));
                }
                System.out.println();
            }
            
            // 리스트를 다 뒤져 봤는데도 충돌나는 MAC 주소가 없을 경우 해당 디바이스를 추가한다.
            // 이는 위에서 찍힌 flag 값이 true 일 경우 같은 MAC으로 false는 다른 MAC 인것으로 판단 
            if(duplication == false)
            {
                System.out.println("MAC 주소가 다름");
                DeviceInfo newDev = new DeviceInfo();   // 새로 추가될 디바이스를 위한 객체 생성
                newDev.setMacAddress(strDevice[0]);     // MAC 주소 추가
                newDev.setAccuracy(strDevice[3]);       // Accuracy 추가
                newDev.setUUID(uniqueDevice[0]);             // UUID
                newDev.setMajor(uniqueDevice[1]);            // Major
                newDev.setMinor(uniqueDevice[2]);            // Minor   
                deviceInfo.add(newDev);                 // 디바이스 리스트에 추가
                System.out.println(newDev.getMacAddress() + " Device 추가");
            }
            duplication = false;        // ture 였을 경우를 위해 값 재설정
            System.out.println();
            
            
            // 디바이스들의 정보를 확인하기 위한 전체 리스트 출력
            System.out.println("----------------\t 디바이스 정보 리스트 \t----------------");
            for (int i = 0; i < deviceInfo.size(); i++)
            {   
                DeviceInfo printDev = deviceInfo.get(i);                    // i 번째 저장된 객체의 디바이스 정보 저장
                System.out.println("["+i+"] Devcie info");                  
                System.out.println("MAC : " + printDev.getMacAddress());
                
                ArrayList<String> accuracy = printDev.getAccuracy();
                System.out.print("Accuracy : ");
                for (int j = 0; j < accuracy.size(); j++)
                {   
                    System.out.print(accuracy.get(j) + " ");                
                }
                System.out.println();
                System.out.println();
            }
            System.out.println();
            
            /*
             * 승차 하차 판단하는 알고리즘 파트
             * 동기화 시간을 1분으로 체크할 경우 Beacon 주기 500ms 설정되어 있어서 1분 동안 120개의 데이터가 잡혀야 함
             * 하지만 실제 Beacon의 숫자가 늘어날 수록 측정되는 속도가 기하급수적으로 느려짐 
             * 테스트에서는 리스트가 10개면 알고리즘을 수행하도록 테스트
             * 1. 이상치 값 조정  
             * 2. 10개의 앞뒤 10% 자름
             * 3. 평균 
             */
            System.out.println("----------------\t Accuraccy 측정 시작 \t----------------");
            for (int i = 0; i < deviceInfo.size(); i++)
            {
                DeviceInfo accurcy = deviceInfo.get(i);         // 리스트에 저장된 Beacon 객체를 하나를 불러온다
                /*********************************************************************************************************
                 * 
                 * 아래 if(accurcy.getAccuracy().size() > 0)    구문 수정해야함  지금은 0개 보다 클때인데 수정할때에는 10개로 변경 또는 그 이상 숫자로 변경해야함
                 * 
                 ********************************************************************************************************* 
                 */
                if(accurcy.getAccuracy().size() > 0)            // 해당 Beacon 객체의 Accuracy 리스트 수가 10개면 승하차 판단을 시작 함
                {
                    System.out.println("Beacon minor : " + accurcy.getMinor());
                    System.out.print(accurcy.getMacAddress() + " " + accurcy.getMinor() + " 에 저장된 accuracy 값 : ");
                    for(int j = 0; j < accurcy.getAccuracy().size(); j++)
                    {   
                        System.out.print(accurcy.getAccuracy().get(j) + " ");                        
                        if (j >= 2)
                        {
                            //System.out.println("더하는 값 : " + accurcy.getAccuracy().get(j));
                            avgAccuracy += Double.parseDouble(accurcy.getAccuracy().get(j));
                            //System.out.println("Current Accuracy sum : " + avgAccuracy);
                            
                            if(accuracyCount == 8)
                            {
                                accuracyCount = 0;
                                break;
                            }
                            accuracyCount++;
                        }
                    }
                    System.out.println();
                    System.out.println(accurcy.getMacAddress() + " " + accurcy.getMinor() + "의 Accuracy sum : " + avgAccuracy);
                    System.out.println(accurcy.getMacAddress() + " " + accurcy.getMinor() + "의 Accuracy avg : " + avgAccuracy / 6);
                    accurcy.setRssi(avgAccuracy / 6);   // 평균 accuracy 값을 해당 객체에 저장
                    accurcy.clearAccuracy();            // 평균을 출력하여 승차하 판단 여부의 값을 출력하면 원소를 모두 삭제함
                    accuracyCount = 0;                  // 초기화
                    avgAccuracy = 0;                    //
                    System.out.println();
                    System.out.println();
                }
            }
            System.out.println();
            System.out.println();
            System.out.println();
            
            // 출력문 확인하려고 늦게 돌아가도록 슬립 넣음 
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
