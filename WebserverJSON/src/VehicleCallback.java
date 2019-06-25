/**
 * @author Andreas
 *
 */

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;


	public class VehicleCallback implements MqttCallback {

		private static final Logger log = null;
		
	    @Override
	    public void connectionLost(Throwable throwable) {
	        System.out.println("Lost Connection to the server !..");
	        System.out.println(throwable.getStackTrace());
	        System.out.println("Cause: " + throwable.getCause());
	        //Logger LOG = LoggerFactory.getLogger(String messageCatalogName, String loggerID);
	        // TODO: Log Server Error
	     // Verbindung neu aufbauen, Abschalten und Fehlerbehandlung ...
	     //   log.error("Verbindung verloren:"+throwable.getCause().getLocalizedMessage());


	    }

	    // TODO: Log message arrived + Fix publish response
	    @Override
	    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
	        /* Check if topic is: /V1/Driver/AuthResponse/ */
	        System.out.println("Msg received on :" + topic + " :" + mqttMessage.toString());
	        if (topic.equals("/SysArch/V1/Driver/AuthRequest/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString());
	        	String authRequest = new String(mqttMessage.getPayload());
	        	VehicleDbModel DriverAuth = new VehicleDbModel();
	        	String authResponse = DriverAuth.authentificateDriver(authRequest);
	        	VehicleComController.publish("/SysArch/V1/Driver/AuthResponse", authResponse, 0);
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

	    @Override
	    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
	    	// TODO: Log AKN message
	    }
	}

