/**
 * @author Andreas
 *
 */

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

	public class VehicleCallback implements MqttCallback {


	    @Override
	    public void connectionLost(Throwable throwable) {
	        System.out.println("Lost Connection to the server !..");
	        System.out.println(throwable.getStackTrace());
	        System.out.println("Cause: " + throwable.getCause());

	        // TODO: Log Severe Error


	    }

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
	        	VehicleComController.publish("/SysArch/V1/Driver/AuthResponse/", authResponse, 2);
	        }
	        
	        if (topic.equals("/SysArch/V1/Driver/LogoutRequest/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString()); 
	        }
	        
	        if (topic.equals("/SysArch/V1/Sensors/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString());
	        	String sensorData = new String(mqttMessage.getPayload());
	        	VehicleDbModel SensorDataV1 = new VehicleDbModel();
	        	Boolean status = SensorDataV1.setVehicleData(sensorData, 1);
	        	System.out.println("Writing sensor values in DB is " + status); 
	        }
	        if (topic.equals("/SysArch/V1/OS/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString()); 
	        }
	        
	    }

	    @Override
	    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

	    }
	}

