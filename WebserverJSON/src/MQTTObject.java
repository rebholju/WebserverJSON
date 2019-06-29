import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * An MQTT object holds the MQTT topic and the
 * MQTT Message. The topic is represented as a string and
 * the message includes a "payload" (the body of the message)
 * represented as a byte[]. The can be accessed by get-Methods.
 */
public class MQTTObject {
	
	 private String topic; 
	 private MqttMessage mqttMessage;

	 /**
		 * Constructs a MQTTObject Object with the specified byte array as a payload,
		 * and all other values set to defaults.
		 * @param topic The topic as String
		 * @param mqttMessage The MqttMessage Object
		 */
	 public MQTTObject(String topic, MqttMessage mqttMessage)
	 {
		 this.topic = topic;
		 this.mqttMessage = mqttMessage;
				 
	 }
	 
	 /**
		 * Returns the topic as a String.
		 *
		 * @return the topic as a String.
		 */
	 public String getTopic()
	 {
		 return this.topic;
	 }
	 
	 /**
		 * Returns the mqttMessage as a MqttMessage.
		 *
		 * @return the mqttMessage as a MqttMessage.
		 */
	 public MqttMessage getMqttMessage()
	 {
		 return this.mqttMessage;
	 }
	 
}
