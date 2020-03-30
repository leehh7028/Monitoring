package DB_Monitoring_Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class PushService {
	private final String SERVER_URL = "http://210.115.227.108/kindergartenBus_server/Final/Push_Notification.php";
	private boolean isSendPush;
	
	public PushService() {
		isSendPush = true;
	}

	public void sendPushMessage(int flag, String id_topic, String title, String message) {
		System.out.println(id_topic + " " + title + " " +message);
		PostURL(flag, id_topic, title, message);
	}
	
	private void PostURL(int flag, String id_topic, String title, String message) {
		try {
			String data  = URLEncoder.encode("flag", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(flag), "UTF-8"); 
			if(flag == 0) { 
	            data += "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id_topic, "UTF-8");
	            data += "&" + URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(title, "UTF-8");
	            data += "&" + URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
			}else {
	            data += "&" + URLEncoder.encode("topic", "UTF-8") + "=" + URLEncoder.encode(id_topic, "UTF-8");
	            data += "&" + URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(title, "UTF-8");
	            data += "&" + URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");
			}

			URL url = new URL(SERVER_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
            conn.setDoOutput(true);  
            conn.setUseCaches(false);  
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			OutputStreamWriter out = null;

			try {
				out = new OutputStreamWriter(conn.getOutputStream());
				out.write(data);
				out.flush();
			} finally {
				if (out != null)
					out.close();
			}
            InputStream is = conn.getInputStream();

		} catch (MalformedURLException e) {
			System.out.println("The URL address is incorrect.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("It can't connect to the web page.");
			e.printStackTrace();
		}
	}
	
	public void setIsSendPush(boolean isSendPush) {
		this.isSendPush = isSendPush;
	}
	
	public boolean getIsSendPush() {
		return this.isSendPush;	
	}
}
