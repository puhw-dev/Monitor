package monitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

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
	
	/// contructor
	public Monitor() throws IOException{
		restServer = new RestServer();
		readParamteres("configure.properties");
		//registerInCatalog();
		// TODO  UDP SERVER
	}
	
	/// private mtehods
	/**
	 * Register this monitor in Catalog.
	 */
	private void registerInCatalog(){	
		try {
			// Construct data
		    String data = URLEncoder.encode("monitorIP", "UTF-8") + "=" + URLEncoder.encode(restServer.server.getAddress().getAddress().getHostAddress(), "UTF-8");
		    //data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");
			URL url = new URL(catalogURL);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    wr.close();
		} catch (Exception e) {
			System.err.println("Monitor Exception in registerInCatalog");
			e.printStackTrace();
		}
	}
	
	/**
     * READABLE parameters in runtime - if necessary expand for more
     */
    private String monitorName, catalogURL;
    private ArrayList<String> usersList = new ArrayList<String>();
    
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
    		while((line = buffer.readLine()) != null)
    		{
    			usersList.add(line); 
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
