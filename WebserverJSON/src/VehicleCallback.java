/**
 * @author Andreas
 *
 */

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

	public class VehicleCallback implements MqttCallback {

	    private boolean isDriverPresent = false;

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
	        }
	        
	        if (topic.equals("/SysArch/V1/Driver/LogoutRequest/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString()); 
	        }
	        
	        if (topic.equals("/SysArch/V1/Sensors/")) 
	        {
	        	System.out.println("Message:" + mqttMessage.toString()); 
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

