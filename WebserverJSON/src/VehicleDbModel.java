import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

//TODO: 
//		Kommentare einf�gen
//		Konsolenausgabe definieren und programmieren + ewtl. Log in String und Datei 

/**
 * @author Andreas
 *
 */
public class VehicleDbModel {

		 private Connection conn = null;
	     private Statement statement = null;
	     private PreparedStatement preparedStatement = null;
	     private ResultSet resultSet = null;
	     private int result=1;
		 
		 public VehicleDbModel()
		 {
			 //https://www.vogella.com/tutorials/MySQLJava/article.html
			 	 
			 try
			 {
			 Class.forName("com.mysql.jdbc.Driver");
			 
			 
			 
//		        $dsn = 'mysql:host=ea-pc165.ei.htwg-konstanz.de; port=3306; dbname=sysarch_w4';
//		        // host=ea-pc165.ei.htwg-konstanz.de;
//		        $dbuser = 'sysarch_w4';
//		        $dbpwd = 'DEF';
		     this.conn = DriverManager.getConnection("jdbc:mysql://ea-pc165.ei.htwg-konstanz.de/sysarch_w4:3306", "sysarch_w4", "DEF");
//			 this.conn = DriverManager.getConnection("jdbc:mysql://localhost/SysArch","root", "");
			 System.out.print("Database is connected !");

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
		
		//TODO: IF-Abfrage state Sensor, wenn "OFF" dann Sensor nicht in DB schreiben + Feedback
		//		Lidar-Sensordaten in DB schreiben
		
		//Set VehicleData
		public boolean setVehicleData(String JSONString, int vehicleNumber)
		{
			
			
			JSONParser jsonParser = new JSONParser();
			try {
				
			JSONObject data = (JSONObject) jsonParser.parse(JSONString);
		    
		    JSONArray sensors = (JSONArray) data.get("sensors");
		    JSONArray passengersarray = (JSONArray) data.get("passengers");
		    JSONObject passengers = (JSONObject) passengersarray.get(0);
		    // nur Driver
		    
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
			
	        String	Date = "00000000T000000Z";
			LocalDateTime parsedDate = LocalDateTime.parse(Date,formatter);
			java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(parsedDate);
			
			
			if(passengers.get("timestamp").toString().equals('0'))
			{
			LocalDateTime parsedDatePassenger = LocalDateTime.parse(passengers.get("timestamp").toString(),formatter);
			java.sql.Timestamp sqlTimestampPassenger = java.sql.Timestamp.valueOf(parsedDatePassenger);
			}
			System.out.println(passengers.get("timestamp").toString());
			
		    for(int i= 0;i<sensors.size();i++)
		    {
		        JSONObject singleSensor = (JSONObject) sensors.get(i);
		        
		        if(singleSensor.get("timestamp").toString().equals('0'))
		        {   	
				 parsedDate = LocalDateTime.parse(singleSensor.get("timestamp").toString(),formatter);
				 sqlTimestamp = java.sql.Timestamp.valueOf(parsedDate);
		        }

			    try {

				    this.preparedStatement = this.conn.prepareStatement("UPDATE vehiclecurrentdata SET value=?, driver=?, timestamp=?, unit=? WHERE vehicleNumber = ? AND sensor = ?");
				    this.preparedStatement.setString(1,singleSensor.get("value").toString());
				    this.preparedStatement.setString(2, passengers.get("name").toString());
				    this.preparedStatement.setTimestamp(3, sqlTimestamp);
				    this.preparedStatement.setString(4, singleSensor.get("unit").toString());
				    this.preparedStatement.setInt(5, vehicleNumber);
				    this.preparedStatement.setString(6, singleSensor.get("name").toString());		    
		            result = this.preparedStatement.executeUpdate();
		            //System.out.println("Wrote into vehiclecurrentdata");
 
			    }
			    catch(Exception ex)
			    {
			    	System.out.println("SensorWert noch nicht vorhanden oder Datenbank nicht verbunden"+ex);
			    	return false;
			    }
			    finally
			    {
			    	preparedStatement.close();
			    	
			    }
		    	
			    
		    	try {
				    	if( result==0 )
				    	{			    		
						    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclecurrentdata(vehicleNumber, sensor, value, timeStamp, driver, unit) values(?, ?, ?, ?, ?)");
						    this.preparedStatement.setInt(1, vehicleNumber);
						    this.preparedStatement.setString(2, singleSensor.get("name").toString());
						    this.preparedStatement.setString(3, singleSensor.get("value").toString());
						    this.preparedStatement.setTimestamp(4, sqlTimestamp);
						    this.preparedStatement.setString(5, passengers.get("name").toString());
						    this.preparedStatement.setString(6, singleSensor.get("unit").toString());
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
				    finally
				    {
				    	preparedStatement.close();
				    	
				    }
					    
		    	try {
		    		
					    this.preparedStatement = this.conn.prepareStatement("INSERT INTO vehiclehistoricaldata(vehicleNumber, sensor, value, timeStamp, driver, unit) values(?, ?, ?, ?, ?)");
					    this.preparedStatement.setInt(1, vehicleNumber);
					    this.preparedStatement.setString(2, singleSensor.get("name").toString());
					    this.preparedStatement.setString(3, singleSensor.get("value").toString());
					    this.preparedStatement.setTimestamp(4, sqlTimestamp);
					    this.preparedStatement.setString(5, passengers.get("name").toString());
					    this.preparedStatement.setString(6, singleSensor.get("unit").toString());
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
		    
		    	preparedStatement.close();
		    	//conn.close();
		    
		    
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
	            		  System.out.println("RFID mehrmals vergeben");
	            	  }
	            	  i++;
	            	}
	            
           }
           catch(Exception ex)
           {
           	System.out.println("Fehler mit Datenbank");
           }
		    finally
		    {
		    	resultSet.close();
		    	preparedStatement.close();
//		    	conn.close();
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
				AuthResponse.put( "id" , rfidID);                    
				AuthResponse.put( "firstName"  , "");           
				AuthResponse.put("lastName"  , "");   
				AuthResponse.put( "authLevel"  , "");
				
				return AuthResponse.toString();
			}
           
     	}
     	catch (Exception err){
     	     System.out.println("Error "+ err);
     	     return null;
     	}
     	
	
		}

		//delete User from actualSensorData
		public boolean logoutRequest(String JSONString)
		{
			
	      	  String username = null;
			JSONParser jsonParser = new JSONParser();
			
			
		  	  
	      	try {
	      		JSONObject data = (JSONObject) jsonParser.parse(JSONString);
				int rfidID = Integer.parseInt(data.get("id").toString());
				
	            try {
	            	
	                try {
	                	
	    	            this.preparedStatement = this.conn.prepareStatement("select username from users where rfidID=?");
	    	            this.preparedStatement.setInt(1,rfidID);
	    	            this.resultSet = this.preparedStatement.executeQuery();
	    	            
	    	            while (resultSet.next()) {
	    	            	int i = 0;
	    	            	  username = resultSet.getString("username");
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
	            	
	            	
		            this.preparedStatement = this.conn.prepareStatement("UPDATE vehiclecurrentdata SET driver=? WHERE driver=?");
		            this.preparedStatement.setString(1,"No Driver authentificated");
		            this.preparedStatement.setString(2,username);
		            this.result = this.preparedStatement.executeUpdate();
		            
		            if(this.result != 0)
		            {
		            	System.out.println("driver deselected");
		            	return true;
		            }
		            else
		            {
		            	return false;
		            }
		            	
	            }
	            catch(Exception ex)
	            {
	            	System.out.println("Fehler mit Datenbank");
	            	return false;
	            }
	            
	      	}
	      	catch (Exception err){
	      	     System.out.println("Error"+ err);
	      	   return false;
	      	}
			
			
			
			
			
		}

		// Get StringArray of existing Vehiclenumbers
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
				System.out.println("Fehler mit Datenbank"+ex);
			}
           
			return vehicles;
		}
	}
