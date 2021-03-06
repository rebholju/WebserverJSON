import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/** Class to write and read user data and vehicle values
 *  into the database. In the class constructor is the connection
 *  to the mysql database specified and by initialization created.
 */
public class VehicleDbModel {

		 // parameters for connection and communication with DB
		 private Connection conn = null;
	     private PreparedStatement preparedStatement = null;
	     private ResultSet resultSet = null;
	     private int result=1;
		 
	     /** Constructor of the class VehicleDbModel. Here is the
	      * DB driver loaded and the connection established.
	      */
		 public VehicleDbModel()
		 {
			 // https://www.vogella.com/tutorials/MySQLJava/article.html
			 	 
			 try
			 {
			 Class.forName("com.mysql.jdbc.Driver");
			 System.out.println("Trying to connect to Database ...");
		     this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/sysarch_w4", "sysarch_w4", "DEF");
		     // this.conn = DriverManager.getConnection("jdbc:mysql://localhost/SysArch","root", "");
			 System.out.println("Database is connected!");

			 }
			 catch(Exception e)
			 {
			 System.out.println("No connection to Database could be established - Error: "+e);
			 }		 
		 }

		/** Method to test the VehicleDbModel functions 
		 * by reading a defined json file  
		*/
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
			
			
			
			try 
			{
				//Read JSON file
				String JSONString = new String(Files.readAllBytes(Paths.get("AuthRequest.json"))); 
	            
	            String Response = authentificateDriver(JSONString);
	            System.out.println(Response);
	            
	        } 
			catch (Exception ex) 
			{
				System.out.println("Fehler String"+ ex);
			}
			
			try 
			{
				//Read JSON file
				String JSONString = new String(Files.readAllBytes(Paths.get("LogoutRequest.json"))); 
	            
	            Boolean Response = logoutRequest(JSONString);
	            System.out.println(Response);
	            
	        } 
			catch (Exception ex) 
			{
				System.out.println("Fehler String"+ ex);
			}

			
			
				//Get Availabile Vehicles  
	            String[] Response = getavailabileVehicles();
	            
	            for(int i =0;i<Response.length;i++)
	            {
	            System.out.println("\n"+Response[i]);
	            }

			
			
	           
		}
		
		/** Method to read the sensor values from the published String and
		 * write the values at into the database.
		 * @param JSONString String from MQTT object
		 * @param vehicleNumber	Integer Number of the corresponding vehicle
		 * @return status Boolean status writing in DB 
		 */
		public boolean setVehicleData(String JSONString, int vehicleNumber)
		{
			// Create new JSON parser to parse String values into JSON Object
			JSONParser jsonParser = new JSONParser();
			String username = null;
			
			
			try {	
			JSONObject data = (JSONObject) jsonParser.parse(JSONString);
		    
		    JSONArray sensors = (JSONArray) data.get("sensors");
		    JSONArray passengersarray = (JSONArray) data.get("passengers");
		    JSONObject passengers = (JSONObject) passengersarray.get(0);
		    // nur Driver
		    
		    // Create timestamp pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
	        String	Date = "11111111T111111Z";
			LocalDateTime parsedDate = LocalDateTime.parse(Date,formatter);
			java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(parsedDate);
			
			// if timestamp is zero, set dummy timestamp
			if(!passengers.get("timeStamp").toString().contentEquals("0"))
			{
			LocalDateTime parsedDatePassenger = LocalDateTime.parse(passengers.get("timeStamp").toString(),formatter);
			java.sql.Timestamp sqlTimestampPassenger = java.sql.Timestamp.valueOf(parsedDatePassenger);
			}
			//System.out.println(passengers.get("timestamp").toString());
			
			
            this.preparedStatement = this.conn.prepareStatement("SELECT username FROM users WHERE vehicle=?");
            this.preparedStatement.setInt(1,1);
            this.resultSet = this.preparedStatement.executeQuery();

            if(this.resultSet != null)
            {
            	while (resultSet.next()) {
	            	int i = 0;
	            	  username = resultSet.getString("username");
	            	  if(i<0)
	            	  {
	            		  System.out.println("Error: multiple users authentificated to Vehicle!");
	            	  }
	            	  i++;
            	}
            }
            if(username == null)
            {
            	username = "no Driver authentificated";
            }
            
            
            
			
			// 
		    for(int i= 0;i<sensors.size();i++)
		    {
		        JSONObject singleSensor = (JSONObject) sensors.get(i);
		        
		        
		    	
		    	if(singleSensor.get("state").toString().contentEquals("On") || singleSensor.get("state").toString().contentEquals("true"))
		    	{
		    		// if timestamp is zero, set dummy timestamp
			        if(!singleSensor.get("timestamp").toString().contentEquals("0"))
			        {   	
					 parsedDate = LocalDateTime.parse(singleSensor.get("timestamp").toString(),formatter);
					 sqlTimestamp = java.sql.Timestamp.valueOf(parsedDate);
			        }
			        
			    try {

					    this.preparedStatement = this.conn.prepareStatement("UPDATE vehiclecurrentdata SET value=?, unit=?, timeStamp=?, driver=? WHERE vehicleNumber = ? AND sensor = ?");
					    this.preparedStatement.setString(1,singleSensor.get("value").toString());
					    this.preparedStatement.setString(2, singleSensor.get("unit").toString());
					    this.preparedStatement.setTimestamp(3, sqlTimestamp);
//					    this.preparedStatement.setString(4, passengers.get("name").toString());
					    this.preparedStatement.setString(4, username);
					    this.preparedStatement.setInt(5, vehicleNumber);
					    this.preparedStatement.setString(6, singleSensor.get("name").toString());	
			            result = this.preparedStatement.executeUpdate();
			            System.out.println("Updating " + singleSensor.get("name") + " in table vehiclecurrentdata was successfull.");
	 
				    }
				    catch(Exception ex)
				    {
				    	System.out.println("Sensor value not existing or database is not connected! 1 "+ ex);
				    	return false;
				    }
				    finally
				    {
				    	preparedStatement.close();
				    	
				    }
			    	
				    
			    	try {
					    	if( result==0 )
					    	{			    		
							    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclecurrentdata(vehicleNumber, sensor, value, unit, timeStamp, driver) values(?, ?, ?, ?, ?, ?)");
							    this.preparedStatement.setInt(1, vehicleNumber);
							    this.preparedStatement.setString(2, singleSensor.get("name").toString());
							    this.preparedStatement.setString(3, singleSensor.get("value").toString());
							    this.preparedStatement.setString(4, singleSensor.get("unit").toString());
							    this.preparedStatement.setTimestamp(5, sqlTimestamp);
//							    this.preparedStatement.setString(6, passengers.get("name").toString());
							    this.preparedStatement.setString(6, username);
							    result = this.preparedStatement.executeUpdate();
							    System.out.println("Writing into the table vehiclecurrentdata was successfull.");
					    	}
			    		}
				    	catch(Exception exception)
				    	{
				    		System.out.println("Sensor value not existing or database is not connected! 2 "+exception);
				    		return false;
				    	}
					    finally
					    {
					    	preparedStatement.close();
					    	
					    }
						    
			    	try {
			    		
						    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclehistoricaldata(vehicleNumber, sensor, value, unit, timeStamp, driver) values(?, ?, ?, ?, ?, ?)");
						    this.preparedStatement.setInt(1, vehicleNumber);
						    this.preparedStatement.setString(2, singleSensor.get("name").toString());
						    this.preparedStatement.setString(3, singleSensor.get("value").toString());
						    this.preparedStatement.setString(4, singleSensor.get("unit").toString());
						    this.preparedStatement.setTimestamp(5, sqlTimestamp);
//						    this.preparedStatement.setString(6, passengers.get("name").toString());   
						    this.preparedStatement.setString(6, username);
						    result = this.preparedStatement.executeUpdate();
	//					    System.out.println(resultSet);
						    System.out.println("Writing values form " + singleSensor.get("name") + " into the table vehiclehistoricaldata was successfull.");
						    
						    
				    	
		    		}
			    	catch(Exception exception)
			    	{
			    		System.out.println("Sensor value not existing or database is not connected! 3 "+exception);
			    		return false;
			    	}

			    }
		    	else if(singleSensor.get("state").toString().contentEquals("Off") || singleSensor.get("state").toString().contentEquals("false")) {
		    		System.out.println("Sensor" + singleSensor.get("name") + " is off!");
		    		
		    	}
		      
		    }
		    	// close precompiled statement and connection to DB
		    	preparedStatement.close();
		    	conn.close();
		    
		    	DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
			    refDatabaseThread.clearFirstObjectofList();
		    
			}
			catch(Exception ex)
			{
				System.out.println("Error while parsing data! " + ex);
			    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
			    refDatabaseThread.clearFirstObjectofList();
				return false;
			}


		    return true;
		}

		/** Method to read the RFID and timestamp from the published String and
		 * check with DB if the values matches a corresponding user.
		 * @param JSONString String from MQTT object
		 * @return authResponse String with id, userdata and timestamp in JSON design 
		 */
		public String authentificateDriver(String JSONString)
		{
		  String id =  null;	
     	  String firstname = null;
     	  String lastname = null;
     	  String role = null;
     	  JSONParser jsonParser = new JSONParser();
     	  
     	  
     	try {
     		JSONObject data = (JSONObject) jsonParser.parse(JSONString);
			// rfidID = Integer.parseInt(data.get("id").toString());
			
           try {
           	
	            this.preparedStatement = this.conn.prepareStatement("SELECT firstname, lastname, role, rfidID FROM users WHERE rfidID=?");
	            this.preparedStatement.setString(1, data.get("id").toString());;
	            this.resultSet = this.preparedStatement.executeQuery();
	            
	            while (resultSet.next()) {
	            	int i = 0;
	            	  id = resultSet.getString("rfidID");
	            	  firstname = resultSet.getString("firstname");
	            	  lastname = resultSet.getString("lastname");
	            	  role = resultSet.getString("role");
	            	  if(role == "10")
	            	  {
	            		  role = "1";
	            	  }
	            	  else
	            	  {
	            		  role = "2";
	            	  }
	            	  if(i<0)
	            	  {
	            		  System.out.println("Error: RFID exist multiple times!");
	            	  }
	            	  i++;
	            	}
	            try
	            {
    	        this.preparedStatement = this.conn.prepareStatement("UPDATE users Set vehicle=? where vehicle=?");
    	        this.preparedStatement.setInt(1,0);
    	        this.preparedStatement.setInt(2,1);
    	        this.preparedStatement.executeUpdate();
    	        
    	        this.preparedStatement.close();
	            	
	            this.preparedStatement = this.conn.prepareStatement("UPDATE users SET vehicle=? WHERE rfidID=?");
	            this.preparedStatement.setInt(1,1);
	            this.preparedStatement.setString(2, id);
	            this.preparedStatement.executeUpdate();
	            }
	            catch(Exception ex)
	            {
	            	System.out.println("RFID does not exist in DB! "+ex);
				    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
				    refDatabaseThread.clearFirstObjectofList();
	            }
	            
			    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
			    refDatabaseThread.clearFirstObjectofList();
	            
           }
           catch(Exception ex)
           {
           	System.out.println("Sensor value not existing or database is not connected! "+ex);
           }
		    finally
		    {
		    	resultSet.close();
		    	preparedStatement.close();
		    	conn.close();
		    }
            JSONObject AuthResponse = new JSONObject();
            
			if(id!=null && firstname!=null && lastname!=null && role!=null)
			{
				
			
			AuthResponse.put( "id" , id);                    
			AuthResponse.put( "firstName"  , firstname);           
			AuthResponse.put("lastName"  , lastname);   
			AuthResponse.put( "authLevel"  , role);
			
			
			return AuthResponse.toString();
			}
			else
			{		
				AuthResponse.put( "id" , data.get("id").toString());                    
				AuthResponse.put( "firstName"  , "");           
				AuthResponse.put("lastName"  , "");   
				AuthResponse.put( "authLevel"  , "");
				
				return AuthResponse.toString();
			}
           
     	}
     	catch (Exception err){
     	     System.out.println("Error while parsing data! " + err);
			    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
			    refDatabaseThread.clearFirstObjectofList();
     	     return null;
     	}
     	
	
		}

		/** Method to read the RFID an timestamp from the published String and
		 * delete driver from actualSensorData in database.
		 * @param JSONString String from MQTT object
		 * @return status Boolean status writing in DB 
		 */
		public boolean logoutRequest(String JSONString)
		{
			
	      	  String username = null;
			JSONParser jsonParser = new JSONParser();
			
			
		  	  
	      	try {
	      		JSONObject data = (JSONObject) jsonParser.parse(JSONString);
//				int rfidID = Integer.parseInt(data.get("id").toString());
				
	            try {
	            	
	                try {
	                	
	    	            this.preparedStatement = this.conn.prepareStatement("select username from users where rfidID=?");
	    	            this.preparedStatement.setString(1,data.get("id").toString());
	    	            this.resultSet = this.preparedStatement.executeQuery();
	    	            
	    	            while (resultSet.next()) {
	    	            	int i = 0;
	    	            	  username = resultSet.getString("username");
	    	            	  if(i<0)
	    	            	  {
	    	            		  System.out.println("Error: RFID exist multiple times!");
	    	            	  }
	    	            	  i++;
	    	            	}
	    	            

	                }
	                catch(Exception ex)
	                {
	                	System.out.println("User does not exist or database is not connected 1! "+ex);
	                }
	            	
    	            this.preparedStatement = this.conn.prepareStatement("UPDATE users Set vehicle=? where rfidID=?");
    	            this.preparedStatement.setInt(1,0);
    	            this.preparedStatement.setString(2,data.get("id").toString());
    	            this.preparedStatement.executeUpdate();
    	            
    	            
    	            this.preparedStatement.close();
    	            
		            this.preparedStatement = this.conn.prepareStatement("UPDATE vehiclecurrentdata SET driver=? WHERE driver=?");
		            this.preparedStatement.setString(1,"No Driver authentificated");
		            this.preparedStatement.setString(2,username);
		            this.result = this.preparedStatement.executeUpdate();
		            
		            if(this.result != 0)
		            {
		            	System.out.println("The driver is deselected from the vehicle.");
		            	
	    	            
					    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
					    refDatabaseThread.clearFirstObjectofList();
		            	
		            	return true;
		            }
		            else
		            {
		            	System.out.println("The driver isn't deselected from the vehicle.");
		            	System.out.println("The driver isn't assigend to the vehicle.");
		            	DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
					    refDatabaseThread.clearFirstObjectofList();
		            	return false;
		            }
		            	
	            }
	            catch(Exception ex)
	            {
	            	DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
				    refDatabaseThread.clearFirstObjectofList();
	            	System.out.println("User does not exist or database is not connected 2! "+ex);
	            	return false;
	            }
	            
	      	}
	      	catch (Exception err){
	      	     System.out.println("Error while parsing data!  "+ err);
				    DatabaseThread refDatabaseThread = DatabaseThread.getinstance();
				    refDatabaseThread.clearFirstObjectofList();
	      	   return false;
	      	}
			
			
			
			
			
		}

		// Get StringArray of existing Vehiclenumbers
		/** Method to read the existing vehicles from the database and
		 *  deliver them as return value in a Stringarray back.
		 * delete driver from actualSensorData in database.
		 * @return vehicles Stringarray of existing vehicles in DB 
		 */
		public String[] getavailabileVehicles()
		{
//			String[] vehicles = new String[10];
			String[] vehicles = {"notSet","notSet","notSet","notSet","notSet","notSet","notSet","notSet","notSet","notSet",};
			int counter2=0;
			try {
           this.preparedStatement = this.conn.prepareStatement("select VehicleNumber from vehiclecurrentdata");
           this.resultSet = this.preparedStatement.executeQuery();
          
           while (resultSet.next()) {
                   int counter = 0;
                   
                   for(int u=0;u<vehicles.length;u++)
                   {
                       if(vehicles[u].toString().equals(this.resultSet.getString("vehicleNumber")))
                       {
                           counter++;
                           
                       }
                   }
                   
                   if(counter==0)
                   {
                       vehicles[counter2] = this.resultSet.getString("vehicleNumber");
                       counter2++;
                   }
               }
           }
			catch(Exception ex)
			{
				System.out.println("Failure getting number of vehicles from database "+ex);
			}
           
			return vehicles;
		}
	}
