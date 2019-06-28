// TODO: Kommentare einfügen
// 		 Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/** This class handles the connection, subscription
 *  publishing and disconnection to the MQTT broker.
 */
public class VehicleComController 
	{

	private String ServerURI = "tcp://localhost";
    private String port = "8884";
    private String broker = ServerURI + ":" + port;
    private String clientId = "Webservice_W4";
    private String userName = "W4"; 
    private String password = "DEF";
    private MemoryPersistence persistence;
	private static MqttClient w4MqttClient;
	private MqttConnectOptions options;
	private MqttCallback callback;
	private static boolean status;	
	
	
	// TODO: Überarbeitung Konstuktoren und Initalisierung
	public VehicleComController() {
		this.status = false;
	}
	/**
     * Constructor to create VehicleComController and set broker 
     * connection parameters.
     * @param ServerURI Servername as String
     * @param port Serverport as String
     * @param clientId individual clientid as String 
     */
	public VehicleComController(String ServerURI, String port, String clientId) 
	{
	    this.ServerURI = ServerURI;
	    this.port = port;
	    this.clientId = clientId;
	    this.broker = ServerURI + ":" + port;
	    this.status = false;
	}
	
	/**
     * Method initializes the Client for the connection to the Broker.
     * The options will be configured, a connection established and to the
     * corresponding topics subscribed.
     * @param topics Stringarray of the topics
     * @param cleanSession Boolean value to determine session cleaning
     * @param userName client connection name as String 
     * @param password client connection password as String
     * @param qos Quality of Service as Integerarray
     */
	public void initializationMQTT(String[] topics, boolean cleanSession, String userName, String password, int[] qos) 
	{
        try {
            persistence = new MemoryPersistence();
            options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setAutomaticReconnect(true);
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            //options.setWill("/SysArch/V1/Driver/AuthResponse/", "Client got disconnected suddently".getBytes(), 0, true);
            w4MqttClient = new MqttClient(broker, clientId, persistence);
            w4MqttClient.setCallback(new VehicleCallback());
            w4MqttClient.setTimeToWait(1000);
            if (connect()) {
            	// connect to broker
            	w4MqttClient.connect(options);
                // subscribe to topics
                //w4MqttClient.subscribe(topics, qos);
            	subscribe(topics, qos);
            } else {
                System.out.println("Client couldn't connect to the Server");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

	}
	
	// TODO: Überarbeitung connect method
	/**
     * Method to connect the Client to the Broker
     * This Method shall be used in the case when not using loop_forever
     *
     * @return true if connection was established, false if not.
     */
    public boolean connect() 
    {
        try {
            if (w4MqttClient != null) {
                System.out.println("Trying to connect..");
                status = true;
                return true;
            } else if (!status){
            	System.out.println("Trying to connect..");
                IMqttToken iMqttToken = w4MqttClient.connectWithResult(options);
                iMqttToken.waitForCompletion();
                boolean connectResponse = iMqttToken.getSessionPresent();
                System.out.println("Connection status: " + connectResponse);
                status = connectResponse;
                return connectResponse;
            }
            return false;
        } catch (MqttException e) {
            e.printStackTrace();
            status = false;
            return false;
        }
    }
	
    /**
     * Method to subscribe to the specified topics on the Broker.
     * @param topic Stringarray of topics
     * @param qos Integerarray of Quality of Service
     * @throws MqttException Exception if could not subscribe or connect
     */
	public void subscribe(String[] topic, int[] qos) throws MqttException {
		if (w4MqttClient != null && w4MqttClient.isConnected()) {
            try {
                w4MqttClient.subscribe(topic, qos);
                System.out.println(" Subscribed to " + topic);
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
     * @param qos   The Quality of Service
     */
    public synchronized static void publish(String topic, String msg, int qos) {
        if (qos < 0 || qos > 2) {
            System.out.println("Invalid QoS: " + qos);
            return;
        }
        else if (status && w4MqttClient != null && w4MqttClient.isConnected()) {
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(qos);
            System.out.println("Publishing message: " + msg);
            try {
                 System.out.println(" connected ");
                 w4MqttClient.publish(topic, message);
                 System.out.println(" Bis hier? ");
            } catch (MqttException e) {
                e.printStackTrace();
                disconnect();
                close();
            }
        } else {
            System.out.println("Connection is lost or client is null");
        }
    }
    
    
    // TODO: Überarbeitung close connection to broker + unsubscribe method
    /**
     * This Method disconnects the Client from the Broker and throws an exception in case smth wrong happened.
     */
    private static void disconnect() {
        try {
            System.out.println("Disconnecting..");
            w4MqttClient.disconnect();
            status = false;

        } catch (MqttException e) {
            System.out.println("Exception when trying to disconnect the client");
            e.printStackTrace();
        }
    }

    /**
     * This Method hard disconnects the Client from the Broker and throws an exception in case smth wrong happened.
     */
    public static void close() {
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

}

