package DB_Monitoring_Service.Custom_Data_Type;

import java.util.ArrayList;
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

public class DeviceInfo
{
    private String UserID;
    private String macAddress;
    private String UUID;
    private String major;
    private String minor;
    private Double rssi;
    private String time;
    private ArrayList<String> Accuracy = new ArrayList<String>();
    


    public String getUserID()
    {
        return UserID;
    }

    public void setUserID(String userID)
    {
        UserID = userID;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;
    }

    public String getUUID()
    {
        return UUID;
    }

    public void setUUID(String uUID)
    {
        UUID = uUID;
    }

    public String getMajor()
    {
        return major;
    }

    public void setMajor(String major)
    {
        this.major = major;
    }

    public String getMinor()
    {
        return minor;
    }

    public void setMinor(String minor)
    {
        this.minor = minor;
    }
    
    public Double getRssi()
    {
        return rssi;
    }

    public void setRssi(Double rssi)
    {
        this.rssi = rssi;
    }
    
    public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
    
    public void setAccuracy(String MAC)
    {
        this.Accuracy.add(MAC);
    }
    
    public ArrayList<String> getAccuracy()
    {
        return Accuracy;
    }
    
    public String getNumAccuracy(int num)
    {
        return Accuracy.get(num);
    }
    
    public void clearAccuracy()
    {
        this.Accuracy.clear();
    }
}
