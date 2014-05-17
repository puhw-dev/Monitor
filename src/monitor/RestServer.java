package monitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * REST server implementation based on com.sun.net.httpserver.HttpServer.
 */
public class RestServer {
	
	private DataBase dataBase;
	public HttpServer server;
	
	/// constructor
	public RestServer () throws IOException {
		//System.out.println(InetAddress.getLocalHost());
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(),8000), 0);
	    server.createContext("/", new MyHandler());
	    server.setExecutor(null); // creates a default executor
	    server.start();
	    
	    dataBase = new DataBase();
	    System.out.println("RestServer: server started");
	}
    
	/// inner class
	/**
	 * Http communication handler class
	 */
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	System.out.println("Request method: " + t.getRequestMethod());
        	System.out.println("Request headers: " + t.getRequestHeaders());
        	System.out.println("Request URI: " + t.getRequestURI());
        	
        	// path parsing
        	URI uri = t.getRequestURI();
        	String [] pathFragments = uri.getPath().split("/");
        	try {
	        	switch (pathFragments.length){
	        		case 8: // path: /hosts/{id]/sensors/{id}/metrics/{id}/measurements
	        			if (!pathFragments[7].equals("measurements"))
	        				throw new Exception("Error, wrong path syntax with 'measurements'");
	        		case 7: // path: /hosts/{id]/sensors/{id}/metrics/{id}
	        			Integer.parseInt(pathFragments[6]);
	        		case 6: // path: /hosts/{id}/sensors/{id}/metrics
	        			if (!pathFragments[5].equals("metrics"))
	        				throw new Exception("Error, wrong path syntax with 'metrics'");
	        		case 5: // path: /hosts/{id]/sensors/{id}
	        			Integer.parseInt(pathFragments[4]);
	        		case 4: // path: /hosts/{id}/sensors
	        			if (!pathFragments[3].equals("sensors"))
	        				throw new Exception("Error, wrong path syntax with 'sensors'");
	        		case 3: // path: /hosts/{id}      			
	        			Integer.parseInt(pathFragments[2]);
	        		case 2: // path: /hosts		
	        			if (!pathFragments[1].equals("hosts"))
	        				throw new Exception("Error, wrong path syntax with 'hosts'");	           		
	        	}
        	} catch (NumberFormatException e){
        		System.err.println("Error with extractig ID from path");
        	} catch (Exception e) {
        		System.err.println(e.getMessage());
        	}
        	
        	
        	// calling REST methods
        	String reqMethod = t.getRequestMethod();
        	String response = null;
        	if (reqMethod.equals("GET"))
        		response = readResource(t, pathFragments);
        	else if (reqMethod.equals("POST"))
        		response = createResource(t, pathFragments);
        	else if (reqMethod.equals("PUT"))
        		response = updateResource(t, pathFragments);
        	else if (reqMethod.equals("DELETE"))
        		response = deleteResource(t, pathFragments);
        	        	
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        
        /**
         * READ operation of CRUD in RESTful system. From HTTP GET method.
         * @param t 
         */
        private String readResource(HttpExchange t, String [] pathFragments){  	
        	
        	JSONObject obj = new JSONObject();
        	JSONObject tmpObj = null;
        	JSONArray list = null;
        	
        	String hostName=null, sensorName=null, metricName=null;
        	DataBase.Host host = null;
        	DataBase.Sensor sensor = null;
        	DataBase.Metric metric = null;
        	
        	//System.out.println("readResource: " + server.getAddress());
        	String monitorIP = server.getAddress().getAddress().getHostAddress();
        	
        	switch (pathFragments.length){
        		case 2: // path: {monitorURI}/hosts	
        			// get hosts list
        			obj.put("name","monitor11");
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
        			//hostID = Integer.parseInt(pathFragments[2]);			
	    			break;
        		case 4: // path: {monitorURI}/hosts/{hostname}/sensors
        			// get sensors list of specific host	
        			hostName = pathFragments[2];
        			host = dataBase.getHost(hostName);
        			obj.put("hostname", host.hostName);
        			obj.put("ip", host.ip);
        			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName);
        			list = new JSONArray();
        			for (DataBase.Sensor tmpSensor : dataBase.getSensors(host.id)){
        				tmpObj = new JSONObject();
        				tmpObj.put("sensorname", tmpSensor.sensorName);
        				//tmpObj.put("owner", value);
        				//tmpObj.put("rpm", value);
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
	    			sensor = dataBase.getSensor(host.id, sensorName);
	    			obj.put("hostname", host.hostName); 
	    			obj.put("sensorname", sensor.sensorName);			
	    			//obj.put("owner", value);
    				//obj.put("rpm", value);
	    			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName);
	    			break;
	    		case 6: // path: {monitorURI}/hosts/{hostname]/sensors/{sensorname}/metrics
	    			// get list metrics list of specific sensor
	    			hostName = pathFragments[2];
	    			sensorName= pathFragments[4];
	    			host = dataBase.getHost(hostName);
	    			sensor = dataBase.getSensor(host.id, sensorName);
	    			obj.put("hostname", host.hostName);
	    			obj.put("sensorname", sensor.sensorName);
	    			//obj.put("owner", value);
    				//obj.put("rpm", value);
	    			obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName);
        			list = new JSONArray();
        			for (DataBase.Metric tmpMetric : dataBase.getMetrics(sensor.id)){
        				tmpObj = new JSONObject();
        				tmpObj.put("name", tmpMetric.name);
        				tmpObj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics/"+tmpMetric.name);
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
	    			sensor = dataBase.getSensor(host.id, sensorName);
	    		 	metric = dataBase.getMetric(sensor.id, metricName);
	    		 	obj.put("hostname", host.hostName);
	    			obj.put("sensorname", sensor.sensorName);
	    			obj.put("metricname", metric.name);
	    			//obj.put("owner", value);
    				//obj.put("rpm", value);
    				obj.put("href", "http://"+monitorIP+"/hosts/"+host.hostName+"/sensors/"+sensor.sensorName+"/metrics/"+metric.name);
	    			break;
	    			
	    		/*case 8: // path: {monitorURI}/hosts/{id]/sensors/{id}/metrics/{id}/measurements
	    			metricID = Integer.parseInt(pathFragments[6]);
	    			obj.put("resource", "measurement");
        			list = new JSONArray();
        			for (DataBase.Measurement measurement : dataBase.getMeasurements(metricID)){
        				tmpObj = new JSONObject();
        				tmpObj.put("id", measurement.id);
        				tmpObj.put("hostID", measurement.metricID);
        				tmpObj.put("time", measurement.time);
        				tmpObj.put("value", measurement.value);
        				list.add(tmpObj);
        			}	
	    			break;
	    			*/
	    	}
        	
        	return obj.toJSONString();
        }
        
        /**
         * CREATE operation of CRUD in RESTful system. From HTTP POST method
         * @param t
         */
        private String createResource(HttpExchange t, String [] pathFragments){
        	String response = null;
        	return response;
        }
        
        /**
         * UPDATE operation of CRUD in RESTful system. From HTTP POST, PUT methods
         * @param t
         */
        private String updateResource(HttpExchange t, String [] pathFragments){
        	String response = null;
        	return response;
        }
        
        /**
         * DELETE operation of CRUD in RESTful system. From HTTP DELETE method.
         * @param t
         */
        private String deleteResource(HttpExchange t, String [] pathFragments){  
        	String response = null;
        	return response;
        }
    }
}
