/**
 * @author Julian Rebholz
 * @author Peter Schmidt
 * @author Andreas Roth
 */

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
		System.out.println("\t\t\tWebservice W4");
		MQTTConV1.initializationMQTT(topics, qos);
				
		// Create DB Thread and give ref to VehicleComController
		DatabaseThread thread = DatabaseThread.getinstance();
				
		// Run Thread
		System.out.println("Start Database Thread ...");
		System.out.println();
		thread.start();
				
		// Loop forever
		while(true) 
		{}			
	}
	
}