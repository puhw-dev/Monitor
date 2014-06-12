package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;

import monitor.DataBase.Metric;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * REST server implementation based on com.sun.net.httpserver.HttpServer.
 */
public class RestServer {
	
	/// private fields
	private DataBase dataBase;
	private HttpServer server;
	private String monitorName = null;
	private String monitorIP = null;
	
	private HashMap<String,CoumpoundMetric> coumpoundMetrics;
	
	
	/// constructor
	public RestServer (String monitorName, DataBase database) throws IOException {
		System.out.println(InetAddress.getLocalHost());
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(),34899), 0);
		MyHandler handler = new MyHandler();
	    server.createContext("/", handler);
	    server.setExecutor(null); // creates a default executor
	    server.start();
	    monitorIP = server.getAddress().getAddress().getHostAddress();
	    this.dataBase = database;
	    this.monitorName = monitorName;
	    coumpoundMetrics = new HashMap<String,CoumpoundMetric>();
	    System.out.println("RestServer: server started");
	    
	    //handler.createResource(null,null);
	}
	
	/// getters
	public String getMonitorIP(){ return monitorIP; }
    
	/// inner class
	/**
	 * Http communication handler class
	 */
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	//test
        	//dataBase.testDataInsertToMetric();
        	///
        	
        	System.out.println("RestServer:: Request method: " + t.getRequestMethod());
        	//System.out.println("RestServer:: Request headers: " + t.getRequestHeaders());
        	System.out.println("RestServer:: Request URI: " + t.getRequestURI());
        	
        	// path parsing
        	URI uri = t.getRequestURI();
        	String [] pathFragments = uri.getPath().split("/");
     
        	System.out.println(pathFragments.length);
        	try {
	        	switch (pathFragments.length){
	        		case 8: // path: /hosts/{hostname}/sensors/{sensorname}/metrics/{metricname}/data
	        			;//if (!pathFragments[7].equals("measurements"))
	        			//	throw new Exception("Error, wrong path syntax with 'measurements'");
	        		case 7: // path: /hosts/{hostname}/sensors/{sensorname}/metrics/{metricname}
	        			//Integer.parseInt(pathFragments[6]);
	        		case 6: // path: /hosts/{hostname}/sensors/{sensorname}/metrics
	        			if (!pathFragments[5].equals("metrics"))
	        				throw new Exception("Error, wrong path syntax with 'metrics'");
	        		case 5: // path: /hosts/{hostname}/sensors/{sensorname}
	        			//Integer.parseInt(pathFragments[4]);
	        		case 4: // path: /hosts/{hostname}/sensors
	        			if (!pathFragments[3].equals("sensors"))
	        				throw new Exception("Error, wrong path syntax with 'sensors'");
	        		case 3: // path: /hosts/{hostname}      			
	        			//Integer.parseInt(pathFragments[2]);
	        		case 2: // path: /hosts		
	        			if (!pathFragments[1].equals("hosts"))
	        				throw new Exception("Error, wrong path syntax with 'hosts'");	           		
	        	}
        	} catch (Exception e) {
        		System.err.println("RestServer:: " + e.getMessage());
        	}
        	
        	
        	// calling REST methods
        	String reqMethod = t.getRequestMethod();
        	if (reqMethod.equals("GET"))
        		readResource(t, pathFragments, uri.getQuery());
        	else if (reqMethod.equals("POST"))
        		createResource(t, pathFragments);
        	else if (reqMethod.equals("PUT"))
        		updateResource(t, pathFragments);
        	else if (reqMethod.equals("DELETE"))
        		deleteResource(t, pathFragments);
        }
        
        
        /**
         * READ operation of CRUD in RESTful system. From HTTP GET method.
         * @param t 
         */
        private void readResource(HttpExchange t, String [] pathFragments, String query){  	
        	
        	JSONObject obj = new JSONObject();
        	JSONObject tmpObj = null;
        	JSONArray list = null;
        	
        	String hostName=null, sensorName=null, metricName=null, dataNumber = null;
        	DataBase.Host host = null;
        	DataBase.Sensor sensor = null;
        	DataBase.Metric metric = null;   	
        	
        	switch (pathFragments.length){
        		case 2: // path: {monitorURI}/hosts	
        			// get hosts list
        			obj.put("name",monitorName);
                	obj.put("href","http://"+monitorIP+"/hosts");
        			list = new JSONArray();
        			for (DataBase.Host tmpHost : dataBase.getHosts()){
        				tmpObj = new JSONObject();
        				tmpObj.put("hostname", tmpHost.hostName);
        				tmpObj.put("ip", tmpHost.ip);
        				tmpObj.put("href", "http://"+monitorIP+"/hosts/"+tmpHost.hostName);
        				list.add(tmpObj);
        			}	
        			obj.put("hosts", list);
        			break;
        		case 3: // path: {monitorURI}/hosts/{hostname}      			
	    			// get specific host
        			hostName = pathFragments[2];
        			host = dataBase.getHost(hostName);
        			obj.put("hostname", host.hostName);
        			obj.put("ip", host.ip);
        			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName);
	    			break;
        		case 4: // path: {monitorURI}/hosts/{hostname}/sensors
        			// get sensors list of specific host	
        			hostName = pathFragments[2];
        			host = dataBase.getHost(hostName);
        			obj.put("hostname", host.hostName);
        			obj.put("ip", host.ip);
        			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName);
        			list = new JSONArray();
        			for (DataBase.Sensor tmpSensor : dataBase.getSensors(host.hostName)){
        				tmpObj = new JSONObject();
        				tmpObj.put("sensorname", tmpSensor.sensorName);
        				tmpObj.put("owner", tmpSensor.login);
        				tmpObj.put("rpm", tmpSensor.rpm);
        				tmpObj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+tmpSensor.sensorName);
        				list.add(tmpObj);
        			}
        			obj.put("sensors", list);
        			break;
	    		case 5: // path: {monitorURI}/hosts/{hostname}/sensors/{sensorname}
	    			// get specific sensor
	    			hostName = pathFragments[2];
	    			sensorName = pathFragments[4];
	    			host = dataBase.getHost(hostName);
	    			sensor = dataBase.getSensor(hostName, sensorName);
	    			obj.put("sensorname", sensor.sensorName);
	    			obj.put("owner", sensor.login);
	    			obj.put("rpm", sensor.rpm);
	    			obj.put("hostname", host.hostName); 
	    			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName);
	    			break;
	    		case 6: // path: {monitorURI}/hosts/{hostname]/sensors/{sensorname}/metrics
	    			// get list metrics list of specific sensor
	    			hostName = pathFragments[2];
	    			sensorName= pathFragments[4];
	    			host = dataBase.getHost(hostName);
	    			sensor = dataBase.getSensor(host.hostName, sensorName);
	    			obj.put("sensorname", sensor.sensorName);
	    			obj.put("hostname", host.hostName);	    			
	    			obj.put("owner", sensor.login);
    				obj.put("rpm", sensor.rpm);
	    			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName);
        			list = new JSONArray();
        			for (DataBase.Metric tmpMetric : dataBase.getMetrics(sensor.id)){
        				tmpObj = new JSONObject();
        				tmpObj.put("name", tmpMetric.metricName);
        				tmpObj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics/"+tmpMetric.metricName);
        				list.add(tmpObj);
        			}
        			obj.put("metrics", list);
	    			break;
	    		case 7: // path: {monitorURI}/hosts/{hostname}/sensors/{sensorname}/metrics/{metricname}
	    			// get specific metric
	    			hostName = pathFragments[2];
	    			sensorName = pathFragments[4];
	    			metricName = pathFragments[6];
	    			host = dataBase.getHost(hostName);
	    			sensor = dataBase.getSensor(host.hostName, sensorName);
	    		 	metric = dataBase.getMetric(sensor.id, metricName);
	    		 	obj.put("hostname", host.hostName);
	    			obj.put("sensorname", sensor.sensorName);
	    			obj.put("metricname", metric.metricName);
	    			obj.put("owner", sensor.login);
    				obj.put("rpm", sensor.rpm);
    				obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics/"+metric.metricName);
	    			break;
	    		case 8: // path: {monitorURI}/hosts/{hostname}/sensors/{sensorname}/metrics/{metric1};{metric2}/data[&n=20]
	    			hostName = pathFragments[2];
	    			host = dataBase.getHost(hostName);
	    			obj.put("hostname", host.hostName);
	    			
	    			sensorName = pathFragments[4];
	    			sensor = dataBase.getSensor(host.hostName, sensorName);
	    			obj.put("sensorname", sensor.sensorName);
	    			obj.put("owner", sensor.login);
	    			obj.put("rpm", sensor.rpm);
	    			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics");

	    			int data = 20;
	    			try{
	    				if (query.split("=").length == 2)
	    					data = Integer.parseInt(query.split("=")[1]);
	    			}
	    			catch (NumberFormatException e){
	    				System.err.println("RestServer:: wrong number in query:"+query);
	    			}
	    			data = data < 1 ? 20 : data > 100 ? 100 : data;
	    			
	    			System.out.println("data: "+data);
    				for(String dd:pathFragments[6].split(";"))
    					System.out.println("metric:" +dd);
	    			
	    			
	    			list = new JSONArray();
	    			for(String tmpMetricName: pathFragments[6].split(";"))
	    			{
	    				metric = dataBase.getMetric(sensor.id, tmpMetricName);
	    				tmpObj = new JSONObject(); tmpObj.put("name", metric.metricName);
	    				list.add(tmpObj);
	    				tmpObj = new JSONObject(); tmpObj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics/"+metric.metricName);
	    				list.add(tmpObj);
		    			JSONArray innerList = new JSONArray();
		    			for (DataBase.Metric tmpMetric : dataBase.getMetricData(sensor.id, metric.metricName, data)){
		    				tmpObj = new JSONObject();
		    				tmpObj.put(tmpMetric.time, tmpMetric.value);
		    				innerList.add(tmpObj);
		    			}
		    			tmpObj = new JSONObject(); tmpObj.put("data", innerList);
		    			list.add(tmpObj);
	    			}
	    			obj.put("metrics", list);

	    			break;
	    	}
        	
        	String response = obj.toJSONString();
        	try {
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
	        	os.write(response.getBytes());
	        	os.close();
			} catch (IOException e) {
				System.err.println("RestServer:readResource: Error with writing response");
				e.printStackTrace();
			}
        }
        
        /**
         * CREATE operation of CRUD in RESTful system. From HTTP POST method
         * @param t
         */
		private void createResource(HttpExchange t, String [] pathFragments){
        	// this method for now is used only to create Coumpund metrics (moving average), sa the URI will be as follow
        	// {monitorURI}/hosts/{hostname}/sensors/{sensorname}/metrics/{metricname}

			BufferedReader br=null;
			String body=null;
			try {
				br = new BufferedReader(new InputStreamReader(t.getRequestBody(),"utf-8"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        	JSONObject obj=(JSONObject)JSONValue.parse(br);
        	System.out.println("RestServer:createResource: body: "+obj);
        	String name = (String)obj.get("cmpoundMetricName");
        	int average = Integer.parseInt((String)obj.get("average"));
        	int rpm = Integer.parseInt((String)obj.get("rpm"));
        	String login = (String)obj.get("login");
        	String password = (String)obj.get("password");
        	
        	String response=null;
        	int statusCode=200; // OK
        	// authentication check
        	DataBase.User user = dataBase.getUser(login);       
        	
        	if (user == null){ 
        		response = "{\"message\":\"Bad Login\"}";}
        	else if (!user.password.equals(password)){
        		response = "{\"message\":\"Bad Password\"}";
        	}
        	if (response != null){
        		statusCode = 401; // Unauthorized
        	}
        	else { // autehntication passed
	        	
	        	// check that name already exists
	        	if(coumpoundMetrics.get(name) != null){ // elements exists
	        		response = "{\"message\":\"Specified name already exists\"}";
	        		statusCode = 500; // Internal Server Error
	        	}
	        	else{ // create new metric
	        		String hostName = pathFragments[2];
	    			String sensorName = pathFragments[4];
	    			String primaryMetricName = pathFragments[6];
	        		int sensorId = dataBase.getSensor(hostName, sensorName).id;
		        	
	        		CoumpoundMetric newMetric = new CoumpoundMetric(name,primaryMetricName,average,rpm,login,password,sensorId);
		        	coumpoundMetrics.put(name, newMetric);
	        		
	        		(new Thread(newMetric)).start();
	        		response = "{\"message\":\"New metric started\"}";
	        		statusCode = 201; // Created
	        	}
        	}
        	
        	// send response
        	try {
        		t.sendResponseHeaders(statusCode, response.length());
        		OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	System.out.println("RestServer:createResource: coumponundMetrics amount:"+coumpoundMetrics.size());      	
        }
        
        /**
         * UPDATE operation of CRUD in RESTful system. From HTTP POST, PUT methods
         * @param t
         */
        private void updateResource(HttpExchange t, String [] pathFragments){
        	String response = null;   	
        }
        
        /**
         * DELETE operation of CRUD in RESTful system. From HTTP DELETE method.
         * @param t
         */
        private void deleteResource(HttpExchange t, String [] pathFragments){
        	// this method for now is used only to delete Coumpund metrics (moving average), sa the URI will be as follow
        	// {monitorURI}/hosts/{hostname}/sensors/{sensorname}/metrics/{coumpoundmetricname}
        	String coumpoundMetricName = pathFragments[6];
        	BufferedReader br=null;
			String body=null;
			try {
				br = new BufferedReader(new InputStreamReader(t.getRequestBody(),"utf-8"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        	JSONObject obj=(JSONObject)JSONValue.parse(br);
        	System.out.println("RestServer:deleteResource: body: "+obj);
        	String login = (String)obj.get("login");
        	String password = (String)obj.get("password");
        	
        	String response=null;
        	int statusCode=200; // OK
        	// authentication check
        	DataBase.User user = dataBase.getUser(login);       
        	
        	if (user == null){ 
        		response = "{\"message\":\"Bad Login\"}";}
        	else if (!user.password.equals(password)){
        		response = "{\"message\":\"Bad Password\"}";
        	}
        	if (response != null){
        		statusCode = 401; // Unauthorized
        	}
        	else { // autehntication passed
        		CoumpoundMetric coumpoundMetric = coumpoundMetrics.get(coumpoundMetricName);
        		if(coumpoundMetric==null){
        			statusCode = 404; // Not Found
        			response = "{\"message\":\"Specified Metric not exists\"}";
            	}
        		else{
        			// stop thread
        			coumpoundMetric.stop();
        			// delete from HashMap
        			coumpoundMetrics.remove(coumpoundMetricName);        			
        			// set response
        			statusCode = 200; // OK
        			response = "{\"message\":\"Specified Metric deleted\"}";       			
        		}
        	}
        	
        	// send response
        	try {
        		t.sendResponseHeaders(statusCode, response.length());
        		OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	System.out.println("RestServer:deleteResource: coumponundMetrics amount:"+coumpoundMetrics.size());   
        }
        
    }
    
    
    /**
     * Class to handle coumpound metrics (moving averages), each in separate thread.
     */
    class CoumpoundMetric implements Runnable {
    	public String name;
    	public String primaryMetricName;
    	public int average;
    	public int rpm;
    	public String login;
    	public String password;
    	public int sensorId;
    	public boolean running = true;
    	//DataBase database;
    	
		public CoumpoundMetric(String name, String primaryMetricName, int average, int rpm, String login,String password, int sensorId) {
			this.name = name;
			this.primaryMetricName =primaryMetricName;
			this.average = average;
			this.rpm = rpm;
			this.login = login;
			this.password = password;
			this.sensorId = sensorId;
			
			//this.database = database;
		}

		@Override
		public void run() {
			System.out.println("RestServer.CoumpoundMetric: New metric created- name:"+this.name+", average:"+this.average+
					", rpm:"+this.rpm+", owner:"+this.login);
			while (running){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int counter=0;
				double value=0;
				for(Metric m : dataBase.getMetricData(this.sensorId, this.primaryMetricName, this.average)){
					++counter;
					value += Double.parseDouble(m.value);
				}
				value /= counter;
				DataBase.Metric metric = dataBase.new Metric(0,this.sensorId,this.name,""+System.currentTimeMillis(),""+value);
				dataBase.addMetric(metric);
			}
			// delete data from db
			dataBase.deleteCompoundMetric(this.name);
			System.out.println("RestServer.CoumpoundMetric: Metric deleted- name:"+this.name+", average:"+this.average+
					", rpm:"+this.rpm+", owner:"+this.login);
		}    	
		
		public void stop(){
			running = false;
			System.out.println("Running="+running);		
		}
    }
}
