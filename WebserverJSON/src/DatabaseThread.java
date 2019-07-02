import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
* Thread to handle the access to the mysql database.
* Therefore a Linkedlist is created where the MQTTObject's could be added 
* and retrieved. The class is an extension of the class Thread from Java.lang.
*/
public class DatabaseThread extends Thread
{
	// Create DatabaseThread instance and LinkedList for MQTT Objects
	private static DatabaseThread instance;
	public LinkedList<MQTTObject> list;
	
	/**
	 * Constructs a new DatabaseThread and creates a LinkedList
	 * for the MQTT Objects.
	 */
	public DatabaseThread()
	{
		list = new LinkedList<MQTTObject>();
	}
	
	/**
	 * Returns the instance of the DatabaseThread class.
	 * If it is null a new Thread will be created.
	 * @return instance 
	 */
	public static DatabaseThread getinstance()
	{
		if(instance == null)
	    {
		instance = new DatabaseThread();
		}
		return instance;
	}
		 
	/**
	 * Method to add the given MQTTObject to the LinkedList.
	 * @param mqttobject Object of class MQTTObject 
	 */
	public void addtoList(MQTTObject mqttobject)
	{
		this.list.add(mqttobject);
	}
	
	/**
	 * Run method of the DatabaseThread. Overrides the run
	 * method from Java.lang.Thread.
	 */
	public void run()
	{
		while(true)
		{
			executeDatabaseAccess();
		}
	}
	
	/**Method to clear first Object in List when Data is written into the Database. 
	 * Called from outside (VehicleDbModel) to guarantee that the Data is written into the Database
	 */
	public void clearFirstObjectofList()
	{
		this.list.removeFirst();
	}
	

	/**
	 * Method to retrieve MQTT object from LinkedList and compare the topic. If the topic string
	 * matches a specified one the String data is retried an the respective method to write into
	 * and read from DB. When an authorization request is send and it is OK the data will be published.
	 */
	public void executeDatabaseAccess()
	{
		try
		{
			// Get MQTT Object from LinkedList and extract the topic and the message
			//MQTTObject object = this.list.removeFirst();
			if(this.list.getFirst() != null)
			{
			MQTTObject object = this.list.getFirst();
			String topic = object.getTopic();
			MqttMessage mqttMessage = object.getMqttMessage();
			System.out.println("topic: " + topic);
			System.out.println("message: " + mqttMessage.toString());
			
		    // Check if the topic is: "/V1/Driver/AuthResponse/"
		    if (topic.equals("/SysArch/V1/Driver/AuthRequest/")) 
		    {
		    	// Convert message into String an give it as parameter to authenticateDriver
		    	// method from VehicleDbModel class
		        String authRequest = new String(mqttMessage.getPayload());
		        VehicleDbModel DriverAuth = new VehicleDbModel();
		        String authResponse = DriverAuth.authentificateDriver(authRequest);
		        System.out.println("Authentification response message: " + authResponse);
		        // If the response String is ok, publish it on the topic
		        if(authResponse != null) 
		        {
		        	VehicleComController.publish("/SysArch/V1/Driver/AuthResponse/", authResponse, 0);
		        	System.out.println("Publishing message to topic was ok.");
		        }
		        else {
		        	System.out.println("Publishing message to topic was not ok.");
		        }
		    }
		        
		    // Check if the topic is: "/V1/Driver/LogoutRequest/"
		    else if (topic.equals("/SysArch/V1/Driver/LogoutRequest/")) 
		    {
		    	// Convert message into String an give it as parameter to logoutRequest
		    	// method from VehicleDbModel class. Return boolean value true or false as status.
		        String logoutRequest = new String(mqttMessage.getPayload());
		        VehicleDbModel DriverLogout = new VehicleDbModel();
		        Boolean status = DriverLogout.logoutRequest(logoutRequest);
		        if (status == true) {
		        	 System.out.println("Logout driver was successfull.");
		        }
		        else {
		        	System.out.println("Logout driver failed!");
		        }
		    }
		        
		    // Check if the topic is: "/SysArch/V1/Sensors/"
		    else if (topic.equals("/SysArch/V1/Sensors/")) 
		    {
		    	// Convert message into String an give it as parameter to setVehicleData
		    	// method from VehicleDbModel class. Return boolean value true or false as status.
		        String sensorData = new String(mqttMessage.getPayload());
		        VehicleDbModel SensorDataV1 = new VehicleDbModel();
		        Boolean status = SensorDataV1.setVehicleData(sensorData, 1);
		        if (status == true) {
		        	System.out.println("Writing all sensor values in DB was successfull.");
		        }
		        else {
		        	System.out.println("Writing all sensor values in DB failed!");
		        }
		    }
		    
		    // Check if the topic is: "/SysArch/V1/OS/"
		    else if (topic.equals("/SysArch/V1/OS/")) 
		    {
		    	clearFirstObjectofList();
		    	System.out.println("Currently there will be no OS data written in DB!");
		    }
			} 
		}
		catch(NoSuchElementException ex)
		{
			//System.out.println("Failure");
		}
	    catch(NullPointerException ex)
		{
			System.out.println("No Item in List to responde to vehicle!");
		}
	}
	 
}