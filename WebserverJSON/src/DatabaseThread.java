import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.LinkedList;
import java.util.NoSuchElementException;

// TODO: Kommentare einf�gen
//		 Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 

public class DatabaseThread extends Thread
	 {
		private static DatabaseThread instance;
		 public LinkedList<MQTTObject> list;
	
		 private DatabaseThread()
		 {
			 list = new LinkedList<MQTTObject>();
		 }
		 public static DatabaseThread getinstance()
		 {
			 if(instance == null)
			 {
				 instance = new DatabaseThread();
			 }
			 return instance;
		 }
		 
		 
		 public void addtoList(MQTTObject mqttobject)
		 {
			 this.list.add(mqttobject);
		 }
		 
		 public void run()
		 {
			 while(true)
			 {
			 executeDatabaseAccess();
			 }
		 }

		 
		 
		 public void executeDatabaseAccess()
		 {
			 try
			 {
			 MQTTObject object = this.list.removeFirst();
			 String topic = object.getTopic();
			 MqttMessage mqttMessage = object.getMqttMessage();
			 
		        /* Check if topic is: /V1/Driver/AuthResponse/ */
		        System.out.println("Msg received on :" + topic + " :" + mqttMessage.toString());
		        if (topic.equals("/SysArch/V1/Driver/AuthRequest/")) 
		        {
		        	System.out.println("Response-Message:" + mqttMessage.toString());
		        	String authRequest = new String(mqttMessage.getPayload());
		        	VehicleDbModel DriverAuth = new VehicleDbModel();
		        	String authResponse = DriverAuth.authentificateDriver(authRequest);
		        	System.out.println("Response-Message:" + authResponse);
		        	if(authResponse != null) {
		        	VehicleComController.publish("/SysArch/V1/Driver/AuthResponse/", authResponse, 0);
		        	}
		        }
		        
		        else if (topic.equals("/SysArch/V1/Driver/LogoutRequest/")) 
		        {
		        	System.out.println("Message:" + mqttMessage.toString());
		        	String logoutRequest = new String(mqttMessage.getPayload());
		        	VehicleDbModel DriverLogout = new VehicleDbModel();
		        	Boolean status = DriverLogout.logoutRequest(logoutRequest);
		        	
		        }
		        
		        else if (topic.equals("/SysArch/V1/Sensors/")) 
		        {
		        	System.out.println("Message:" + mqttMessage.toString());
		        	String sensorData = new String(mqttMessage.getPayload());
		        	VehicleDbModel SensorDataV1 = new VehicleDbModel();
		        	Boolean status = SensorDataV1.setVehicleData(sensorData, 1);
		        	System.out.println("Writing sensor values in DB is " + status); 
		        }
		        else if (topic.equals("/SysArch/V1/OS/")) 
		        {
		        	System.out.println("Message:" + mqttMessage.toString()); 
		        }
		 }
		catch(NoSuchElementException ex)
			 {
				//System.out.println("Failure");
			 }
	    catch(NullPointerException ex)
			 {
				System.out.println("No Item in List to responde to vehicle");
			 }
		 }
	 
	 }