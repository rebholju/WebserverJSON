import org.eclipse.paho.client.mqttv3.IMqttToken;
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
	private MqttClient w4MqttClient;
	private MqttConnectOptions options;
	private boolean status;	
	
	
	/**
     * Constructor to create VehicleComController and set broker 
     * connection status parameter.
     */
	public VehicleComController() {
		status = false;
	}
	
	/**
     * Method initializes the Client for the connection to the Broker.
     * The options will be configured, a connection established and to the
     * corresponding topics subscribed.
     * @param topics Stringarray of the topics
     * @param qos Quality of Service as Integerarray
     */
	public void initializationMQTT(String[] topics, int[] qos) 
	{
        try {
        	System.out.println("Initialization MQTT ...");
            persistence = new MemoryPersistence();
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setUserName(this.userName);
            options.setPassword(this.password.toCharArray());
            w4MqttClient = new MqttClient(broker, clientId, persistence);
            w4MqttClient.setCallback(new VehicleCallback());
            w4MqttClient.setTimeToWait(1000);
            // connect to broker
            connect();
            if(status == true) {
            	// subscribe to topics
            	subscribe(topics, qos);
            }
            else if(status != true) {
            	System.out.println("Ongoing trying to connect max 10 times!");
            	for (int i = 0; i<=10; i++) {
            		connect();
            		if(status == true) {
            			// subscribe to topics
            			subscribe(topics, qos);
            			break;
            			}
            	}
            }
            else {
            	System.out.println("No connection could be established!");
            	System.out.println("Please check you're internet connection or the broker configuration.");
            }
        	} 
        	catch (MqttException e) {
        		e.printStackTrace();
        	}
	}
	
	/**
     * Method to connect the Client to the Broker
     * This Method shall be used in the case when not using loop_forever
     *
     * @return true if connection was established, false if not.
     * @throws MqttException Exception if could not connect
     */
    public void connect() throws MqttException
    {
        try {
            if (w4MqttClient != null) {
                System.out.println("Connecting to broker ...");
                w4MqttClient.connect(options);
                status = true;
            } 
            else if (!status) {
            	System.out.println("Connecting to broker ...");
                IMqttToken iMqttToken = w4MqttClient.connectWithResult(options);
                iMqttToken.waitForCompletion();
                boolean connectionResponse = iMqttToken.getSessionPresent();
                System.out.println("Connection status: " + connectionResponse);
                status = connectionResponse;
            }
            System.out.println("MQTT-Client couldn't connect to the broker!");
            status =  false;
        } 
        catch (MqttException e) {
            e.printStackTrace();
            status = false;
        }
    }
	
    /**
     * Method to subscribe to the specified topics on the Broker.
     * @param topic Stringarray of topics
     * @param qos Integerarray of Quality of Service
     * @throws MqttException Exception if could not subscribe
     */
	public void subscribe(String[] topic, int[] qos) throws MqttException {
		if (w4MqttClient != null && w4MqttClient.isConnected()) {
            try {
                w4MqttClient.subscribe(topic, qos);
                System.out.println(" Subscribed to topics:");
                System.out.print("\t" + topic[0] + "\n\t" + topic[1] + "\n\t" + topic[2] + "\n\t" + topic[3]);
                System.out.println("With Quality of Service: " + qos[0]);
            } catch (MqttException e) {
                e.printStackTrace();
                disconnect();
            }
		}
		else {
			System.out.println("Couldn't subscribe to topics!");
		}
	}
	
	/**
     * This Method publishes a specific msg on a specific topic.
     *
     * @param topic The topic where the msg should be published
     * @param msg   The content that should be published
     * @param qos   The Quality of Service
     * @throws MqttException Exception if could not publish
     */
    public synchronized void publish(String topic, String mqttmsg, int qos) throws MqttException{
        if (qos < 0 || qos > 2) {
            System.out.println("Invalid Quality of Service: " + qos);
        }
        else if (status && w4MqttClient != null && w4MqttClient.isConnected()) {
            MqttMessage message = new MqttMessage(mqttmsg.getBytes());
            message.setQos(qos);
            System.out.println("Publishing to broker ...");
            System.out.println("Publishing topic: " + topic);
            System.out.println("Publishing message: " + mqttmsg);
            System.out.println("Publishing Quality of Service: " + qos);
            try {
                 w4MqttClient.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();
                disconnect();
            }
        } else {
            System.out.println("Couldn't publish on the topic!");
        }
    }
    
    /**
     * This Method disconnects the Client from the Broker and closes the connection.
     * @throws MqttException Exception if could not disconnect
     */
    private void disconnect() throws MqttException {
        try {
            System.out.println("Disconnecting from Broker ...");
            w4MqttClient.disconnect();
            status = false;

        } catch (MqttException e) {
            System.out.println("Exception when trying to disconnect from broker!");
            e.printStackTrace();
        }
        if(status == false) {
        	try {
        		System.out.println("Closing the Client ...");
        		w4MqttClient.close();
        		System.out.println("Client sucessfull disconnected from broker.");
        	} 	
        	catch (MqttException e) {
            System.out.println("Exception when trying to close the client!");
            e.printStackTrace();
        	}
        }
    }

}

