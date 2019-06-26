import org.eclipse.paho.client.mqttv3.MqttMessage;

//TODO: 
//		Kommentare einfügen
//		Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 

/**
 * @author Andreas
 *
 */
public class MQTTObject {
	
	 private String topic; 
	 private MqttMessage mqttMessage;

	 public MQTTObject(String topic, MqttMessage mqttMessage)
	 {
		 this.topic = topic;
		 this.mqttMessage = mqttMessage;
				 
	 }
	 
	 public String getTopic()
	 {
		 return this.topic;
	 }
	 
	 public MqttMessage getMqttMessage()
	 {
		 return this.mqttMessage;
	 }
	 
}
