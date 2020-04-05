
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
     * ������ �Ǵ� ���� �����ϱ�
     */
    public void isRidding(boolean isRidding, String major, String minor)
    {
        // firebase���� �����ϰ��� �ϴ� ID �о����
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

        // �����ϰ��� �ϴ� DATA�� MAP�� ���·� ����
        Map<String, Object> childrenStateUpdateData = new HashMap<String, Object>();

        // ���� ž������ ������ ��� ������ ��
        state = "���� ž��";
        childrenStateUpdateData.put("isRidding", isRidding);

        // ���� ������ ������ ��� �Ʒ� ������ ���� Ȱ��
        /*
         * state = "���� ����"; 
         * childrenStateUpdateData.put("isRidding", false);
         */
        // �ش� �����͸� firebase�� update
        db.collection("children").document(childrenID).update(childrenStateUpdateData);
    }

    
    /*
     * log ���
     */
    public void Log() throws InterruptedException, ExecutionException
    {
        // ���¸� �����ϸ鼭 log�� ����ϱ� ���� �ð� ����� ������.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy��/MM��/dd��/HH:mm");
        Date dateTime = new Date();

        String chidrenUpdateTime = dateFormat.format(dateTime);
        // yyyy��/MM��/dd��/HH:mm�� ���·� ����� ��¥ ���
        System.out.println(chidrenUpdateTime);        
        
        // ����� ��¥�� /�� �����Ͽ� firebase�����͸� �����Կ� �־ �ʿ��� �����ͷ� ����        
        String[] updateDateList = chidrenUpdateTime.split("/");

        // ������Ʈ �� �ð��� log�� �߰��ϱ� ���� ���� �ش��Ͽ� ���� log���� �ִ��� Ȯ��
        // �ش����� ���� ����
        String year = updateDateList[0];
        String month = updateDateList[1];
        String day = updateDateList[2];
        String time = updateDateList[3];

        // ���� 0���� �����ϴ� ��� �տ� 0 �� �����ϴ� �۾�
        if (month.startsWith("0"))
        {
            month = month.substring(1, 3);
        }
        // ���� 0���� �����ϴ� ��� �տ� 0�� �����ϴ� �۾�
        if (day.startsWith("0"))
        {
            day = day.substring(1, 3);
        }

        String updatedLogDate = year + " " + month + " " + day;
        String updateLogTime = time;
        System.out.println("Update date : " + updatedLogDate);
        System.out.println("Update time : " + updateLogTime);

        // �ش� ��¥�� log �˻�
        ApiFuture<DocumentSnapshot> getLogDocuments = db.collection("children")
                        .document(childrenID)
                        .collection("log")
                        .document(updatedLogDate)
                        .get();

        // �ش� ��¥�� ID�� ������ Documents�� �ִٸ� ���� �����͸� �����ϱ� ���� �о��
        if (getLogDocuments.get().exists())
        {
            // ������ �ִ� �����͸� �о�´�..
            String getLogTime = getLogDocuments.get().get(updatedLogDate).toString();
            String getLogTimeStr = getLogTime.substring(1, getLogTime.length() - 1);
            System.out.println("str : " + getLogTimeStr);
            
            // �о�� �����͸� List�� ��ȯ�Ѵ�. ", "�� �������� split ��
            String[] getLogTimeList = getLogTimeStr.split(", ");
            List timeList = new ArrayList();
            
            // ���� �����͸� timeList�� �߰�
            timeList.addAll(Arrays.asList(getLogTimeList));
            
            // �߰��ϰ��� �ϴ� �����͸� �߰�
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
            // �ش� ��¥�� ID�� ������ Documents�� ���ٸ� ���� ù��° �����ͷ� �߰�
            // �߰��ϰ��� �ϴ� ���¸� MAP���� ����
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
     * ���� ���� ���� ���� 
     */
    public void busSensorDataModify() throws InterruptedException, ExecutionException
    {
        // firebase���� �����ϰ��� �ϴ� ID �о����
        ApiFuture<QuerySnapshot> busRef = db.collection("bussensors")
                        .whereEqualTo("uuid", "e2c56db5-dffb-48d2-b060-d0f5a71096e0")
                        .whereEqualTo("busNum", "1").get();

        List<QueryDocumentSnapshot> busDocuments = busRef.get().getDocuments();
        String busDocID = busDocuments.get(0).getId();

        // �߰��ϰ��� �ϴ� ������ Map ���·� �����
        Map<String, Object> busUpdateData = new HashMap<String, Object>();
        
        // ���� ��¥ �����
        SimpleDateFormat busDateFormat = new SimpleDateFormat("yyyy�� MM�� dd��");
        Date busDateTime = new Date();

        String getBusUpdateTime = busDateFormat.format(busDateTime);
        busUpdateData.put("humi", 55);
        busUpdateData.put("illum", 55);
        busUpdateData.put("temp", 26.1);
        busUpdateData.put("time", getBusUpdateTime);

        db.collection("bussensors").document(busDocID).update(busUpdateData);
    }

}


