package monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * REST server implementation based on com.sun.net.httpserver.HttpServer.
 */
public class RestServer {
	
	/// private fields
	private DataBase dataBase;
	private HttpServer server;
	private String monitorName = null;
	private String monitorIP = null;
	
	
	/// constructor
	public RestServer (String monitorName, DataBase database) throws IOException {
		System.out.println(InetAddress.getLocalHost());
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(),34899), 0);
	    server.createContext("/", new MyHandler());
	    server.setExecutor(null); // creates a default executor
	    server.start();
	    monitorIP = server.getAddress().getAddress().getHostAddress();
	    this.dataBase = database;
	    this.monitorName = monitorName;
	    System.out.println("RestServer: server started");
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
        	String response = null;
        	if (reqMethod.equals("GET"))
        		response = readResource(t, pathFragments, uri.getQuery());
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
        private String readResource(HttpExchange t, String [] pathFragments, String query){  	
        	
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
        				tmpObj.put("owner", dataBase.getUser(tmpSensor.userID).login);
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
	    			obj.put("owner", dataBase.getUser(sensor.userID).login);
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
	    			obj.put("owner", dataBase.getUser(sensor.userID).login);
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
	    			obj.put("owner", dataBase.getUser(sensor.userID).login);
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
	    			obj.put("owner", dataBase.getUser(sensor.userID).login);
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
