
package DB_Monitoring_Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.Api;

public class FirebaseStorage
{
    Firestore db;
    String childrenID;
    String state;

    public FirebaseStorage()
    {

    }

    public void FirebaseConnet()
    {
        try
        {
            InputStream serviceAccount = new FileInputStream("safe-zone-child-firebase-adminsdk-mq2cq-8ea2101a36.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options =
                            new FirebaseOptions.Builder().setCredentials(credentials).build();
            FirebaseApp.initializeApp(options);

            db = FirestoreClient.getFirestore();
            System.out.println("Firebase init success");
        }
        catch (Exception e)
        {
            // TODO: handle exception
            System.out.println("Firebase init fail");
        }
    }

    
    /*
     * 승하차 판단 여부 변경하기
     */
    public void isRidding(boolean isRidding, String major, String minor)
    {
        // firebase에서 수정하고자 하는 ID 읽어오기
        ApiFuture<QuerySnapshot> childrenRef = db.collection("children")
                        .whereEqualTo("uuid", "e2c56db5-dffb-48d2-b060-d0f5a71096e0")
                        .whereEqualTo("major", "40001")
                        .whereEqualTo("minor", "30530")
                        .get();
        
        List<QueryDocumentSnapshot> documents = null;
        try
        {
            documents = childrenRef.get().getDocuments();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        childrenID = documents.get(0).getId();
        System.out.println("target Document ID : " + childrenID);

        // 변경하고자 하는 DATA를 MAP의 형태로 저장
        Map<String, Object> childrenStateUpdateData = new HashMap<String, Object>();

        // 차량 탑승으로 변경할 경우 데이터 셋
        state = "차량 탑승";
        childrenStateUpdateData.put("isRidding", isRidding);

        // 차량 하차로 변경할 경우 아래 데이터 셋을 활용
        /*
         * state = "차량 하차"; 
         * childrenStateUpdateData.put("isRidding", false);
         */
        // 해당 데이터를 firebase에 update
        db.collection("children").document(childrenID).update(childrenStateUpdateData);
    }

    
    /*
     * log 기록
     */
    public void Log() throws InterruptedException, ExecutionException
    {
        // 상태를 변경하면서 log를 기록하기 위해 시간 양식을 셋팅함.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년/MM월/dd일/HH:mm");
        Date dateTime = new Date();

        String chidrenUpdateTime = dateFormat.format(dateTime);
        // yyyy년/MM월/dd일/HH:mm의 형태로 저장된 날짜 출력
        System.out.println(chidrenUpdateTime);        
        
        // 저장된 날짜를 /로 구분하여 firebase데이터를 수정함에 있어서 필요한 데이터로 수정        
        String[] updateDateList = chidrenUpdateTime.split("/");

        // 업데이트 된 시간을 log에 추가하기 위해 먼저 해당일에 기존 log들이 있는지 확인
        // 해당일을 먼저 구성
        String year = updateDateList[0];
        String month = updateDateList[1];
        String day = updateDateList[2];
        String time = updateDateList[3];

        // 월이 0으로 시작하는 경우 앞에 0 을 제거하는 작업
        if (month.startsWith("0"))
        {
            month = month.substring(1, 3);
        }
        // 일이 0으로 시작하는 경우 앞에 0을 제거하는 작업
        if (day.startsWith("0"))
        {
            day = day.substring(1, 3);
        }

        String updatedLogDate = year + " " + month + " " + day;
        String updateLogTime = time;
        System.out.println("Update date : " + updatedLogDate);
        System.out.println("Update time : " + updateLogTime);

        // 해당 날짜로 log 검색
        ApiFuture<DocumentSnapshot> getLogDocuments = db.collection("children")
                        .document(childrenID)
                        .collection("log")
                        .document(updatedLogDate)
                        .get();

        // 해당 날짜를 ID로 가지는 Documents가 있다면 기존 데이터를 보관하기 위해 읽어옴
        if (getLogDocuments.get().exists())
        {
            // 기존에 있던 데이터를 읽어온다..
            String getLogTime = getLogDocuments.get().get(updatedLogDate).toString();
            String getLogTimeStr = getLogTime.substring(1, getLogTime.length() - 1);
            System.out.println("str : " + getLogTimeStr);
            
            // 읽어온 데이터를 List로 변환한다. ", "를 기준으로 split 함
            String[] getLogTimeList = getLogTimeStr.split(", ");
            List timeList = new ArrayList();
            
            // 기존 데이터를 timeList에 추가
            timeList.addAll(Arrays.asList(getLogTimeList));
            
            // 추가하고자 하는 데이터를 추가
            timeList.add(time + "/" + state);
            Map<String, Object> newUpdateTime = new HashMap<String, Object>();
            newUpdateTime.put(updatedLogDate, timeList);
            db.collection("children")
                .document(childrenID)
                .collection("log")
                .document(updatedLogDate)
                .update(newUpdateTime);
        } 
        else
        {
            // 해당 날짜를 ID로 가지는 Documents가 없다면 가장 첫번째 데이터로 추가
            // 추가하고자 하는 형태를 MAP으로 저장
            List timeList = new ArrayList();
            timeList.add(time + "/" + state);
            Map<String, Object> newUpdateTime = new HashMap<String, Object>();
            newUpdateTime.put(updatedLogDate, timeList);
            db.collection("children")
                .document(childrenID)
                .collection("log")
                .document(updatedLogDate)
                .set(newUpdateTime);
        }
    }

    /*
     * 버스 센서 정보 수정 
     */
    public void busSensorDataModify() throws InterruptedException, ExecutionException
    {
        // firebase에서 수정하고자 하는 ID 읽어오기
        ApiFuture<QuerySnapshot> busRef = db.collection("bussensors")
                        .whereEqualTo("uuid", "e2c56db5-dffb-48d2-b060-d0f5a71096e0")
                        .whereEqualTo("busNum", "1").get();

        List<QueryDocumentSnapshot> busDocuments = busRef.get().getDocuments();
        String busDocID = busDocuments.get(0).getId();

        // 추가하고자 하는 데이터 Map 형태로 만들기
        Map<String, Object> busUpdateData = new HashMap<String, Object>();
        
        // 오늘 날짜 만들기
        SimpleDateFormat busDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date busDateTime = new Date();

        String getBusUpdateTime = busDateFormat.format(busDateTime);
        busUpdateData.put("humi", 55);
        busUpdateData.put("illum", 55);
        busUpdateData.put("temp", 26.1);
        busUpdateData.put("time", getBusUpdateTime);

        db.collection("bussensors").document(busDocID).update(busUpdateData);
    }

}


