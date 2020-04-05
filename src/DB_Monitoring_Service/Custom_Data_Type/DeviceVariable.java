package DB_Monitoring_Service.Custom_Data_Type;

public class DeviceVariable
{
    private String macaddress;
    private double rssi;
    private double txpower;
    private double accuracy;
    private String time;

    public String getMacaddress()
    {
        return macaddress;
    }

    public void setMacaddress(String macaddress)
    {
        this.macaddress = macaddress;
    }

    public double getRssi()
    {
        return rssi;
    }

    public void setRssi(double rssi)
    {
        this.rssi = rssi;
    }

    public double getTxpower()
    {
        return txpower;
    }

    public void setTxpower(double txpower)
    {
        this.txpower = txpower;
    }

    public double getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy(double accuracy)
    {
        this.accuracy = accuracy;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

}
