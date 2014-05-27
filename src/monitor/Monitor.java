package monitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import monitor.DataBase.User;

public class Monitor {

	public static void main(String[] args) {
		try {
			new Monitor();
		}
		catch (Exception e){
			System.err.println("Monitor Exception: "+e.getMessage());
		}
	}
	
	/// private fields
	private RestServer restServer;
	private DataBase dataBase;
	
	/// contructor
	public Monitor() throws IOException{
		dataBase = new DataBase();
		restServer = new RestServer(dataBase);
		readParamteres("configure.properties");
		//registerInCatalog();
		//changeMonitorEntryInCatalog();
		//deleteMonitorEntryInCatalog();
	}
	
	/// private mtehods
	/**
	 * Register this monitor in Catalog.
	 */
	private void registerInCatalog(){	
		try {
			// Construct data
			String data = "{\"name\":\""+monitorName+"\",\"ip\":\""+restServer.monitorIP+":8889\"}";
			String urlString = catalogURL+"/monitors";
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    wr.close();
		    int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + urlString);
			System.out.println("Response Code : " + responseCode);   
		} catch (Exception e) {
			System.err.println("Monitor Exception in registerInCatalog");
			e.printStackTrace();
		}
	}
	
	/**
	 * Change Monitor ip in catalog
	 */
	private void changeMonitorEntryInCatalog(){
		try {
			// Construct data
			String data = "{\"ip\":\"89.68.69.22:8889\"}";
			String urlString = catalogURL+"/monitors/Real_Monitor_that_should_work";
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    wr.close();
		    int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + urlString);
			System.out.println("Response Code : " + responseCode);   
		} catch (Exception e) {
			System.err.println("Monitor Exception in registerInCatalog");
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete Monitor Entry in Catalog
	 */
	private void deleteMonitorEntryInCatalog(){
		try {
			// Construct data
			String urlString = catalogURL+"/monitors/"+monitorName;
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setDoOutput(true);
		    int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'DELETE' request to URL : " + urlString);
			System.out.println("Response Code : " + responseCode);   
		} catch (Exception e) {
			System.err.println("Monitor Exception in registerInCatalog");
			e.printStackTrace();
		}
	}
	
	/**
     * READABLE parameters in runtime - if necessary expand for more
     */
    private String monitorName, catalogURL;
    
    /**
     * READ parameters in runtime
     * @param fileName
     */
    private void readParamteres(String fileName)
    {
    	String line = "";
    	
    	FileReader file = null;
    	try 
    	{
    		file = new FileReader(fileName);
		} 
    	catch (FileNotFoundException e)
    	{
    		System.err.format("Exception occurred trying to open '%s'.", fileName);
			e.printStackTrace();
			System.exit(1);
		}
    	
    	BufferedReader buffer = new BufferedReader(file);
    	try 
    	{
    		monitorName = buffer.readLine();
    		catalogURL = buffer.readLine();
    		List<User> users = dataBase.getUsers();
    		boolean exists = false;
    		while((line = buffer.readLine()) != null)
    		{
    			String [] userData = line.split(":");
    			exists = false;
    			for(User user : users){
    				if (user.login.equals(userData[0])){
    					exists = true;
    					break;
    				}
    			}
    			
    			if (!exists)
    				dataBase.addUser(dataBase.new User (userData[0],userData[1])); 
    	    }
    	} 
    	catch (IOException e)
    	{
    		System.err.format("Exception occurred trying to read '%s'.", fileName);
    		e.printStackTrace();
	        System.exit(2);
    	}
	    
	    try 
	    {
	    	file.close();
	    } 
	    catch (IOException e) 
	    {
	    	System.err.format("Exception occurred trying to close '%s'.", fileName);
	    	e.printStackTrace();
	        System.exit(3);
	    }
	    
    }
}
