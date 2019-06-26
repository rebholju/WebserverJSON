
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.rmi.Naming;

import java.sql.*;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

//import javax.print.attribute.DateTimeSyntax;

//import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

//TODO: 
//		Kommentare einfügen
//		Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 

		public class Main {
		
			public static void main(String[] args) 
			{
//
//
//				VehicleDbModel refVehicleDbModel = new VehicleDbModel();
//				refVehicleDbModel.Main();
				VehicleComController MQTTConV1 = new VehicleComController();
				String[] topics = {"/SysArch/V1/Driver/AuthRequest/", "/SysArch/V1/Driver/LogoutRequest/", "/SysArch/V1/Sensors/", "/SysArch/V1/OS/"};
				int[] qos = {0,0,0,0};
				MQTTConV1.initializationMQTT(topics, true, "W4", "DEF", qos);
				
				DatabaseThread thread = DatabaseThread.getinstance();
				
				thread.start();
				
				while(true) {}
				
				//Test geht nut wenn es sich beim Zweiten element des MQTTObject auch um ein string handelt
//		    	MQTTObject object = new MQTTObject("HALLO", "EIN TEST");
//		    	 thread.addtoList(object);
//			    	 object = new MQTTObject("HALLO2", "EIN TEST2");
//			    	 thread.addtoList(object);
//			    	 
				

			}
		}





