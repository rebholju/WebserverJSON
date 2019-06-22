/**
 * @author Andreas
 * Class to Communicate with MQTT broker and to send an receive data form the vehicles
 */

import java.io.InputStream;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class VehicleComController 
	{

	private String ServerURI = "tcp://iot.eclipse.org";
    private String port = "1883";
    private String broker = ServerURI + ":" + port;
    private String clientId = "Webservice_W4";
    private String userName = "w4"; 
    private String password = "";
    private MemoryPersistence persistence;
	private MqttClient w4MqttClient;
	private MqttConnectOptions options;
	private MqttCallback callback;
	private boolean status;	
	
	public VehicleComController() {
		this.status = false;
	}
	
	public VehicleComController(String ServerURI, String port, String clientId, MqttCallback callback) 
	{
		this.callback = callback;
	    this.ServerURI = ServerURI;
	    this.port = port;
	    this.clientId = clientId;
	    this.broker = ServerURI + ":" + port;
	    this.status = false;
	}
	
	public void init(String topicFilter, boolean cleanSession, String userName, String password) 
	{
        try {
            persistence = new MemoryPersistence();
            options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setAutomaticReconnect(true);
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            options.setWill("/V1/Driver/AuthRequest/", "Client got disconnected suddently".getBytes(), 2, true);
            options.setWill("/V1/Driver/LogoutRequest/", "Client got disconnected suddently".getBytes(), 2, true);
            options.setWill("/V1/Sensors/", "Client got disconnected suddently".getBytes(), 2, true);
            options.setWill("/V1/OS/", "Client got disconnected suddently".getBytes(), 2, true);
            w4MqttClient = new MqttClient(broker, clientId, persistence);
            w4MqttClient.setCallback(new VehicleCallback());
            if (connect()) {
                // subscribe to topics
                w4MqttClient.subscribe(topicFilter, 2);
            } else {
                System.out.println("Client couldn't connect to the Server");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

	}
	
	/**
     * Method to connect the Client to the Broker
     * This Method shall be used in the case when not using loop_forever
     *
     * @return true if connection was established, false if not.
     */
    public boolean connect() 
    {
        try {
            if (w4MqttClient != null && !status) {
                System.out.println("Trying to connect..");
                w4MqttClient.connect(options);
                status = true;
                return true;
            } else {
                System.out.println("Client is null !");
                status = false;
                return false;
            }

        } catch (MqttException e) {
            e.printStackTrace();
            status = false;
            return false;
        }
    }
	
/*	public void open(String ServerURI, String userName, char[] password) throws MqttException 
	{
		w4MqttClient=new MqttClient(ServerURI, MqttClient.generateClientId(), null);
		options=new MqttConnectOptions();
		options.setUserName(userName);
		options.setPassword(password);
		options.setCleanSession(true);
		w4MqttClient.setCallback(callback);
		w4MqttClient.connect(options);
	}
*/
    
	public void subscribe(String topicpattern, int qos) throws MqttException {
		w4MqttClient.subscribe(topicpattern, 2);
		if (w4MqttClient != null) {
            try {
                w4MqttClient.connect(options);
                w4MqttClient.subscribe(topicpattern, qos);
                System.out.println(" Subscribed to " + topicpattern);
                w4MqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
                disconnect();
                close();
            }
}
	}
	
	/**
     * This Method publishes a specific msg on a specific topic.
     *
     * @param topic The topic where the msg should be published
     * @param msg   The content that should be published
     */
    public void publish(String topic, String msg, int qos) {
        if (qos < 0 || qos > 2) {
            System.out.println("Invalid QoS: " + qos);
            return;
        }
        if (status && w4MqttClient != null) {
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(qos);
            System.out.println("Publishing message: " + msg);
            try {
            	 w4MqttClient.connect(options);
                 System.out.println(" connected ");
                 w4MqttClient.publish(topic, message);
                 w4MqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
                disconnect();
                close();
            }
        } else {
            System.out.println("Connection is lost or client is null");
        }
    }
    
    /**
     * This Method disconnects the Client from the Broker and throws an exception in case smth wrong happened.
     */
    private void disconnect() {
        try {
            System.out.println("Disconnecting..");
            w4MqttClient.disconnect();
            status = false;

        } catch (MqttException e) {
            System.out.println("Exception when trying to disconnect the client");
            e.printStackTrace();
        }
    }

    public void close() {
        if (status) {
            disconnect();
            try {
                System.out.println("Closing Client..");
                w4MqttClient.close();
            } catch (MqttException e) {
                System.out.println("Exception when trying to close the client");
                e.printStackTrace();
            }
        } else {
            try {
                w4MqttClient.close();
            } catch (MqttException e) {
                System.out.println("Exception when trying to close the client");
                e.printStackTrace();
            }
        }
}
/*	
	public static InputStream startMQTT() throws MqttException {
		TopicInputStream tis = new TopicInputStream();
		MQTTConnection con = new MQTTConnection(tis);
		con.open("tcp://ea-pc165:8883", "w4", "DEF".toCharArray());
		con.subscribe("/SysArch/w4");
		return tis;
	        }
	....
			InputStream is;
			try {
				is = startMQTT();
			} catch (MqttException e) {
				e.printStackTrace();
				return;
			}
			laser = new LaserScanner(new DataInputStream(is));
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	import org.eclipse.paho.client.mqttv3.MqttClient;
        import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;
        import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

        public class MqttPublishSample {

        public static void main(String[] args) {

            String topic        = "MQTT Examples";
            String content      = "Message from MqttPublishSample";
            int qos             = 2;
            String broker       = "tcp://iot.eclipse.org:1883";
            String clientId     = "JavaSample";
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: "+broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: "+content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
                System.exit(0);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
        }
    }
	*/
}

