package DB_Monitoring_Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import DB_Monitoring_Service.Custom_Data_Type.DeviceInfo;
import DB_Monitoring_Service.Custom_Data_Type.DeviceVariable;


public class mysql
{

    private String DBName = ""; // db이름
    private String url = "jdbc:mysql://210.115.227.108:3306/";// mysql 서버
    private String strUser = ""; // 계정 id
    private String strPassword = ""; // 계정 패스워드
    private String strMySQLDriver = "com.mysql.cj.jdbc.Driver"; // 드라이버 이름
    private Connection con = null; // db 연결
    private Statement stmt = null; // db 삽입, 출력등에 사용
    private ArrayList list = null;


    public mysql()
    {
        try
        {
            Class.forName(strMySQLDriver); // db드라이버설정

        }
        catch (Exception e)
        { // try
            System.out.println(e.getMessage());
        }
    }


    private void getUrl(String DBName)
    {
        url += DBName + "?characterEncoding=UTF-8&serverTimezone=UTC";
    }


    private void connect()
    {
        try
        {
            __Setting__ ss = new __Setting__();

            DBName = ss.getDBName();
            strUser = ss.getStrUser();
            strPassword = ss.getStrPassword();
            getUrl(DBName);
            con = (Connection) DriverManager.getConnection(url, strUser, strPassword);
            // System.out.println("Mysql DB Connection.");
            stmt = con.createStatement();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // 연결

    }


    private void disconnect()
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////


    // 승하차시간, 유저 id, 승하차여부
    public void input_timetable(String id, String time, int isridding)
    {
        connect();
        try
        {
            String sql = "insert into timetable_tb values('" + id + "','" + time + "','"
                            + String.valueOf(isridding) + "')";
            // 쿼리문을 삽입 후 데이터 삽입
            int rss = stmt.executeUpdate(sql); // executeUpdate은 int 형이다.

        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
    }


    public ArrayList<DeviceVariable> select_device_variable_info_tb()
    {
        connect();
        try
        {
            String sql = "select * from device_variable_info_tb";
            ResultSet rs = stmt.executeQuery(sql);
            DeviceVariable dv = null;
            list = new ArrayList<DeviceVariable>();
            while (rs.next())
            {
                dv = new DeviceVariable();
                dv.setMacaddress(rs.getString("macaddress"));
                dv.setRssi(rs.getDouble("rssi"));
                dv.setTxpower(rs.getDouble("txpower"));
                dv.setAccuracy(rs.getDouble("accuracy"));
                dv.setTime(rs.getString("time"));

                list.add(dv);
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return list;
    }

    public String device_variable_info_tb()
    {
        connect();
        String variable = "";
        try
        {
            // String sql = "Select temp from sensor_data ORDER BY time DESC limit 1;";
            String sql = "SELECT * FROM `device_variable_info_tb` ORDER BY `time` DESC limit 1;";
            ResultSet rs = stmt.executeQuery(sql);
            DeviceVariable dv = null;
            while (rs.next())
            {
                /*
                 * dv = new DeviceVariable(); dv.setMacaddress(rs.getString("macaddress"));
                 * dv.setRssi(rs.getDouble("rssi")); dv.setTxpower(rs.getDouble("txpower"));
                 * dv.setAccuracy(rs.getDouble("accuracy")); dv.setTime(rs.getString("time"));
                 */
                variable += rs.getString("macaddress") + " " + rs.getDouble("rssi") + " "
                                + rs.getDouble("txpower") + " " + rs.getDouble("accuracy");
                
                //System.out.print(rs.getString("macaddress") + " ");
                //System.out.print(rs.getString("rssi") + " ");
                //System.out.print(rs.getDouble("txpower") + " ");
                //System.out.println(rs.getDouble("accuracy"));
                
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return variable;
    }


    public ArrayList<DeviceInfo> select_device_info()
    {
        connect();
        try
        {
            // String sql = "SELECT DISTINCT macaddress FROM device_variable_info_tb";
            String sql = "SELECT di.macAddress AS macaddress, ul.id AS id FROM device_unique_info_tb AS di JOIN userslist_tb AS ul ON di.major = ul.major AND di.minor = ul.minor";
            ResultSet rs = stmt.executeQuery(sql);
            DeviceInfo di = null;
            list = new ArrayList<String>();
            while (rs.next())
            {
                di = new DeviceInfo();
                di.setMacAddress((rs.getString("macAddress")));
                di.setUserID((rs.getString("id")));

                list.add(di);
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }

        return list;
    }
    
    /*
     * Beacon UUID를 가져와야 함 
     */
    public String select_device_uuid(String MAC)
    {
        connect();
        String uuid = "";
        try 
        {
            String sql = "SELECT * FROM device_unique_info_tb WHERE macAddress LIKE '" + MAC + "%'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                if(MAC.equals(rs.getString("macAddress")))
                {    
                    uuid = rs.getString("UUID") + " " + rs.getString("major") + " " + rs.getString("minor"); 
                    break;                    
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        return uuid;
    }


    public ArrayList<DeviceVariable> select_recent(int maximum, String macAddress)
    {
        connect();
        try
        {
            String sql = "Select accuracy, time from device_variable_info_tb where macaddress='"
                            + macAddress + "' ORDER BY time DESC Limit " + String.valueOf(maximum);
            ResultSet rs = stmt.executeQuery(sql);
            DeviceVariable dv = null;
            list = new ArrayList<DeviceVariable>();
            while (rs.next())
            {
                dv = new DeviceVariable();
                dv.setAccuracy(rs.getDouble("accuracy"));
                dv.setTime(rs.getString("time"));

                list.add(dv);
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return list;
    }


    public ArrayList<String> select_Tokens()
    {
        connect();
        try
        {
            String sql = "Select token from userslist_tb";
            ResultSet rs = stmt.executeQuery(sql);
            list = new ArrayList<String>();
            while (rs.next())
            {
                list.add(rs.getString("token"));
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return list;
    }


    /*
     * 2020-04-05 error modify
     * 
     * error info = com.mysql.jdbc.ResultSetImpl.checkRowPos 온도 측정
     * 
     */
    public Double select_recent_temperature()
    {
        connect();
        double temp = 0.0;
        try
        {
            String sql = "Select temp from sensor_data ORDER BY time DESC limit 1;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                temp = rs.getDouble("temp");
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return temp;
    }


    /*
     * 조도 값 받아 오기
     */
    public Double select_recent_illumination()
    {
        connect();
        double illum = 0.0;
        try
        {
            String sql = "Select illum from sensor_data ORDER BY time DESC limit 1;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                illum = rs.getDouble("illum");
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return illum;
    }

    /*
     * 습도 값 받아오기
     */
    public Double select_recent_humidity()
    {
        connect();
        double humi = 0.0;
        try
        {
            String sql = "Select humi from sensor_data ORDER BY time DESC limit 1;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                humi = rs.getDouble("humi");
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return humi;
    }



    ///////
    public ArrayList<DeviceVariable> Test_interference()
    {
        connect();
        try
        {
            String sql = "Select * from device_variable_info_tb";
            ResultSet rs = stmt.executeQuery(sql);
            DeviceVariable dv = null;
            list = new ArrayList<DeviceVariable>();
            while (rs.next())
            {
                dv = new DeviceVariable();
                dv.setMacaddress(rs.getString("macaddress"));
                dv.setRssi(rs.getDouble("rssi"));
                dv.setTxpower(rs.getDouble("txpower"));
                dv.setAccuracy(rs.getDouble("accuracy"));
                dv.setTime(rs.getString("time"));

                list.add(dv);
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            disconnect();
        }
        return list;
    }



}
