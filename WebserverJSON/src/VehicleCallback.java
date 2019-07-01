import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Implementation of the MqttClalback class to enable the MQTT
 * Callbacks for logging the connection status and trigger 
 * an event when a message is publish on the initialized topics. 
 */
	public class VehicleCallback implements MqttCallback {
		
		/**
		 * Overrides the connectionLost method from MqttCallback. 
		 * This method is called when the connection to the server is lost. If there
		 * is no cause then it was a clean disconnect. The connectionLost callback
		 * will be invoked if registered and run on the thread that requested
		 * shutdown e.g. receiver or sender thread. If the request was a user
		 * initiated disconnect then the disconnect token will be notified.
		 * 
		 * @param cause the reason behind the loss of connection.
		 */
	    @Override
	    public void connectionLost(Throwable cause) {
	        System.out.println("Callback connectionLost: The Connection to the broker was disconnected suddenly!");
	        System.out.println("Cause: " + cause.getCause());
	        System.out.println("Message: " + cause.getLocalizedMessage());



	    }

	    /**
	     * Overrides the messageArrived method from MqttCallback.
		 * This method is called when a message arrives on a topic. Messages are
		 * only added to the queue for inbound messages if the client is not
		 * quiescing.
		 * 
		 * @param topic name of the topic on the message was published to
		 * @param mqttMessage the actual message.
		 * @throws Exception if a terminal error has occurred, and the client should be shut down.
		 */
	    @Override
	    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
	    	System.out.println("Message on subscribed topic \"" + topic + "\" arrived.");
	    	// Initialize new MQTTObject Object and give as parameter
	    	// the topic and the MQTT message 
	    	// Initialize new DB Thread and add MQTT object to LinkedList
	    	if(mqttMessage.isRetained() == false) {
	    	MQTTObject object = new MQTTObject(topic, mqttMessage);
	    	DatabaseThread thread = DatabaseThread.getinstance();
	    	thread.addtoList(object);
	    	}
	    	else {
	    		mqttMessage.clearPayload();
	    		System.out.println("Retained message arrived and was deleted.");
	    	}
	    }

	    /**
		 * Overrides the deliveryComplete method from MqttCallback.
		 * Called when delivery for a message has been completed, and all
		 * acknowledgments have been received. For QoS 0 messages it is
		 * called once the message has been handed to the network for
		 * delivery. For QoS 1 it is called when PUBACK is received and
		 * for QoS 2 when PUBCOMP is received. The token will be the same
		 * token as that returned when the message was published.
		 *
		 * @param MqttDeliveryToken the delivery token associated with the message.
		 */
	    @Override
	    public void deliveryComplete(IMqttDeliveryToken MqttDeliveryToken) {
	    	System.out.println("Message on published topic was deliverd succesfull to broker.");
	    	System.out.println("Message ID: " + MqttDeliveryToken.getMessageId());
	    	System.out.println("Is complete: " + MqttDeliveryToken.isComplete());
	    }

	    
}

