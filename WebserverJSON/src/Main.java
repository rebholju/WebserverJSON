//TODO: 
//		Kommentare einfügen
//		Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 

/**
 * The MAIN Class contains the initialization of
 * the VehicleComController Class and the DatabaseThread
 * Class. When the Thread is started the programm is running
 * in a forever Loop.
 */
public class Main {
	
	/**
	 * Main method and start point of the program.
	 * Here are the initialization of the classes and
	 * the start of the DB Thread.
	 * @param args The Stringarray as programm start parameter  
	 */
	public static void main(String[] args) 
	{
		// Initialization ComController
		VehicleComController MQTTConV1 = new VehicleComController();
		String[] topics = {"/SysArch/V1/Driver/AuthRequest/", "/SysArch/V1/Driver/LogoutRequest/", "/SysArch/V1/Sensors/", "/SysArch/V1/OS/"};
		int[] qos = {0,0,0,0};
		MQTTConV1.initializationMQTT(topics, true, "W4", "DEF", qos);
				
		// Create DB Thread and get Instance
		// TODO: Classe umschreiben um ComController Referenz zu übergeben
		DatabaseThread thread = DatabaseThread.getinstance();
				
		// Run Thread
		thread.start();
				
		// Loop forever
		while(true) 
		{}			
	}
	
}