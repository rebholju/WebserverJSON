
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import javax.print.attribute.DateTimeSyntax;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


	 class VehicleDbModel{	
		
		 private Connection conn = null;
	     private Statement statement = null;
	     private PreparedStatement preparedStatement = null;
	     private ResultSet resultSet = null;
	     private int result=0;
		 
		 public VehicleDbModel()
		 {
			 //https://www.vogella.com/tutorials/MySQLJava/article.html
			 	 
			 try
			 {
			 Class.forName("com.mysql.jdbc.Driver");
			 this.conn = DriverManager.getConnection("jdbc:mysql://localhost/SysArch","root", "");
			 System.out.print("Database is connected !");

//	            this.conn.close();
			 }
			 catch(Exception e)
			 {
			 System.out.print("Do not connect to DB - Error:"+e);
			 }
			 
		 }

		 //Call Functions
		public void Main()
		{	
			//JSON parser object to parse read file
			
			
			try
			{
				String JSONString = new String(Files.readAllBytes(Paths.get("WikiBeispiel.json"))); 
	            setVehicleData(JSONString,1);
	            
	        } catch (Exception ex) 
			{
	        	System.out.println("Fehler String"+ ex);
	        }
			
			
			
			try (FileReader reader = new FileReader("AuthRequest.json"))
			{
				//Read JSON file
				String JSONString = new String(Files.readAllBytes(Paths.get("AuthRequest.json"))); 
	            
	            String Response = authentificateDriver(JSONString);
	            System.out.println(Response);
	            
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
		//Set VehicleData
		public boolean setVehicleData(String JSONString, int vehicleNumber)
		{
			JSONParser jsonParser = new JSONParser();
			try {
			JSONObject data = (JSONObject) jsonParser.parse(JSONString);
		    
		    JSONArray sensors = (JSONArray) data.get("Sensors");
		    JSONObject passengers = (JSONObject) data.get("Passengers");
		    
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
			LocalDateTime parsedDatePassenger = LocalDateTime.parse(passengers.get("timestamp").toString(),formatter);
		//	java.sql.Timestamp sqlTimestampPassenger = java.sql.Timestamp.valueOf(parsedDatePassenger);
		
		    for(int i= 0;i<sensors.size();i++)
		    {
		        JSONObject singleSensor = (JSONObject) sensors.get(i);

				LocalDateTime parsedDate = LocalDateTime.parse(singleSensor.get("timestamp").toString(),formatter);
				java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(parsedDate);

			    try {
 
				    this.preparedStatement = this.conn.prepareStatement("UPDATE vehiclecurrentdata SET value=?, driver=?, timestamp=? WHERE vehicleNumber = ? AND sensor = ?");
				    this.preparedStatement.setString(1,singleSensor.get("value").toString());
				    this.preparedStatement.setString(2, passengers.get("name").toString());
				    this.preparedStatement.setTimestamp(3, sqlTimestamp);
				    this.preparedStatement.setInt(4, vehicleNumber);
				    this.preparedStatement.setString(5, singleSensor.get("name").toString());		    
		            result = this.preparedStatement.executeUpdate();
		            //System.out.println("Wrote into vehiclecurrentdata");
  
			    }
			    catch(Exception ex)
			    {
			    	System.out.println("SensorWert noch nicht vorhanden oder Datenbank nicht verbunden"+ex);
			    	return false;
			    }
		    	
			    
		    	try {
				    	if( result==0 )
				    	{			    		
						    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclecurrentdata(vehicleNumber, sensor, value, timeStamp, driver) values(?, ?, ?, ?, ?)");
						    this.preparedStatement.setInt(1, vehicleNumber);
						    this.preparedStatement.setString(2, singleSensor.get("name").toString());
						    this.preparedStatement.setString(3, singleSensor.get("value").toString());
						    this.preparedStatement.setTimestamp(4, sqlTimestamp);
						    this.preparedStatement.setString(5, passengers.get("name").toString());
						    result = this.preparedStatement.executeUpdate();
//						    System.out.println(resultSet);
						    System.out.println("Wrote into vehiclecurrentdata");
				    	}
		    		}
			    	catch(Exception exception)
			    	{
			    		System.out.println("Fehler 2te "+exception);
			    		return false;
			    	}
				    
		    	try {
		    		
					    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclehistoricaldata(vehicleNumber, sensor, value, timeStamp, driver) values(?, ?, ?, ?, ?)");
					    this.preparedStatement.setInt(1, vehicleNumber);
					    this.preparedStatement.setString(2, singleSensor.get("name").toString());
					    this.preparedStatement.setString(3, singleSensor.get("value").toString());
					    this.preparedStatement.setTimestamp(4, sqlTimestamp);
					    this.preparedStatement.setString(5, passengers.get("name").toString());
					    result = this.preparedStatement.executeUpdate();
//					    System.out.println(resultSet);
					    System.out.println("Wrote into vehiclehistoricaldata");
			    	
	    		}
		    	catch(Exception exception)
		    	{
		    		System.out.println("Fehler 2te "+exception);
		    		return false;
		    	}
			     
		      
		    }
		    
			}
			catch(Exception ex)
			{
				System.out.println("Fehler beim Parsen" + ex);
				return false;
			}

		    return true;
		}

		//authentificate Driver
		public String authentificateDriver(String JSONString)
		{
      	  String firstname = null;
      	  String lastname = null;
      	  String role = null;
      	  JSONParser jsonParser = new JSONParser();
      	  
      	  
      	try {
      		JSONObject data = (JSONObject) jsonParser.parse(JSONString);
			int rfidID = Integer.parseInt(data.get("id").toString());
			
            try {
            	
	            this.preparedStatement = this.conn.prepareStatement("select firstname, lastname, role from users where rfidID=?");
	            this.preparedStatement.setInt(1,rfidID);
	            this.resultSet = this.preparedStatement.executeQuery();
	            
	            while (resultSet.next()) {
	            	int i = 0;
	            	  firstname = resultSet.getString("firstname");
	            	  lastname = resultSet.getString("lastname");
	            	  role = resultSet.getString("role");
	            	  if(i<0)
	            	  {
	            		  System.out.println("RFID mehrmals vergeben");
	            	  }
	            	  i++;
	            	}
	            
            }
            catch(Exception ex)
            {
            	System.out.println("Fehler mit Datenbank");
            }
            JSONObject AuthResponse = new JSONObject();
			if(firstname!=null && lastname!=null && role!=null)
			{
			
			
			AuthResponse.put( "id" , rfidID);                    
			AuthResponse.put( "firstName"  , firstname);           
			AuthResponse.put("lastName"  , lastname);   
			AuthResponse.put( "authLevel"  , role);
			
			
			return AuthResponse.toString();
			}
			else
			{		
				AuthResponse.put( "id" , null);                    
				AuthResponse.put( "firstName"  , null);           
				AuthResponse.put("lastName"  , null);   
				AuthResponse.put( "authLevel"  , null);
				
				return AuthResponse.toString();
			}
            
      	}
      	catch (Exception err){
      	     System.out.println("Error"+ err);
      	     return null;
      	}
	
		}


	}

		
		

		
		public class Main {
		
			public static void main(String[] args) 
			{

				//VehicleDbModel refVehicleDbModel = new VehicleDbModel();
				//refVehicleDbModel.Main();
				VehicleComController MQTTConV1 = new VehicleComController();
				String[] topics = {"/SysArch/V1/Driver/AuthRequest/", "/SysArch/V1/Driver/LogoutRequest/", "/SysArch/V1/Sensors/", "/SysArch/V1/OS/"};
				int[] qos = {0,0,0,0};
				MQTTConV1.initializationMQTT(topics, true, "W4", "DEF", qos);
				
				while(true) {}
			}
		}





