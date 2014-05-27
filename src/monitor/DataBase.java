package monitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.sqlite.JDBC;

/**
 * Class to handle SQLite data base.
 * There are 4 tables in db: Host, Sensor, Metric and Measurement.  
 * @author Mietek
 */
public class DataBase {
	
	public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:monitor.db";
    
    private Connection conn;
    
    /// constructor
    /**
     * Create/connect to database monitor.db and invoke method to create tables.
     */
    public DataBase() {
        try {
            Class.forName(DataBase.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Lack of JDBC driver");
            e.printStackTrace();
        }
 
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Problem with open connection");
            e.printStackTrace();
        }
        createTables();
    }
    
    /// private methods
    /**
     * Create tables if not exists.
     * @return true if no error occurs.
     */
    private boolean createTables()  {
    	String createUser = "CREATE TABLE IF NOT EXISTS user"
    			+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT, password TEXT)";
        String createHost = "CREATE TABLE IF NOT EXISTS host"
        		+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, hostname TEXT, ip TEXT)";
        String createSensor = "CREATE TABLE IF NOT EXISTS sensor"
        		+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, sensorname TEXT, hostid  INTEGER)";
//        String createMetric = "CREATE TABLE IF NOT EXISTS metric"
//                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, sensorID  INTEGER, time TEXT, metricname TEXT, value TEXT)";
        String createMetric = "CREATE TABLE IF NOT EXISTS metric"
        		+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, sensorID  INTEGER, metricname TEXT)";
        String createMeasurement = "CREATE TABLE IF NOT EXISTS measurement"
        		+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, metricID INTEGER, time TEXT, value REAL)";
        try {
            Statement stat = conn.createStatement();
            stat.execute(createUser);
            stat.execute(createHost);
            stat.execute(createSensor);
            stat.execute(createMetric);
            //stat.execute(createMeasurement);
        } catch (SQLException e) {
            System.err.println("Error in creating tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    /// public api
    /**
     * Get list of available hosts.
     * @return hosts list.
     */
    public List<Host> getHosts(){
    	List<Host> hosts = new LinkedList<Host>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM host");
            int id;
            String hostName, ip;
            while(result.next()) {
                id = result.getInt("id");
                hostName = result.getString("hostname");
                ip = result.getString("ip");            
                hosts.add(new Host(id, hostName, ip));
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }     
        return hosts;
    }
    
    /**
     * Get specific host by id.
     * @param hostId host id
     * @return requested host or null if there are no host with such id in db.
     */
    public Host getHost(int hostID){
    	Host host = null;
    	try {
        	Statement stat = conn.createStatement();
        	ResultSet result = stat.executeQuery("SELECT * FROM host where id="+hostID);
        	if(result.next()){
        		host = new Host(result.getInt("id"), result.getString("hostname"), result.getString("ip"));
        	}
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    	return host;
    }
    
    /**
     * Get specific host by name
     * @param hostName
     * @return host
     */
    public Host getHost(String hostName){
    	Host host = null;
    	try {
        	Statement stat = conn.createStatement();
        	ResultSet result = stat.executeQuery("SELECT * FROM host where hostname='"+hostName+"'");
        	if(result.next()){
        		host = new Host(result.getInt("id"), result.getString("hostname"), result.getString("ip"));
        	}
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    	return host;
    }
    
    /**
     * Get metrics list of specific host
     * @param hostID host id
     * @return sensor list
     */
    public List<Sensor> getSensors(int hostID){
    	List<Sensor> sensors = new LinkedList<Sensor>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM sensor where hostID="+hostID);
            int id;
            String sensorName;
            while(result.next()) {
                id = result.getInt("id");
                sensorName = result.getString("sensorname");
                sensors.add(new Sensor(id, sensorName, hostID));
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }      
        return sensors;
    }
    
    /**
     * Get specific sensor of specific host, by hostID and sensorName
     * @param hostID
     * @param sensorName
     * @return sensor
     */
    public Sensor getSensor(int hostID, String sensorName){
    	Sensor sensor = null;
    	try {
        	Statement stat = conn.createStatement();
        	ResultSet result = stat.executeQuery("SELECT * FROM sensor WHERE sensorname='"+sensorName+"'"
        			+" AND hostID="+hostID);
        	if(result.next()){
         		sensor = new Sensor(result.getInt("id"), result.getString("sensorName"), result.getInt("hostid"));
        	}
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    	return sensor;
    }
    
    /**
     * Get metrics list of specific sensor
     * @param sensorID host id
     * @return metrics list
     */
    public List<Metric> getMetrics(int sensorID){
    	List<Metric> metrics = new LinkedList<Metric>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM metric where sensorID="+sensorID);
            int id;
            String name;
            while(result.next()) {
                id = result.getInt("id");
                name = result.getString("metricname");
                metrics.add(new Metric(id, sensorID, name));
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }      
        return metrics;
    }
    
    /**
     * Get sepcific metric by sensorID and metricName
     * @param sensorID
     * @param metricName
     * @return
     */
    public Metric getMetric(int sensorID, String metricName){
    	Metric metric = null;
    	try {
        	Statement stat = conn.createStatement();
        	ResultSet result = stat.executeQuery("SELECT * FROM metric WHERE metricname='"+metricName+"'"
        			+" AND sensorID="+sensorID);
        	if(result.next()){
         		metric = new Metric(result.getInt("id"), result.getInt("sensorid"), result.getString("metricname"));
        	}
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    	return metric;
    }
    
    /**
     * Get measurements list of specific metric
     * @param metricID metric id
     * @return measurements list
     */
    public List<Measurement> getMeasurements(int metricID){
    	List<Measurement> measurements = new LinkedList<Measurement>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM measurement where metricID="+metricID);
            int id;
            String time;
            double value;
            while(result.next()) {
                id = result.getInt("id");
                time = result.getString("time");
                value = result.getDouble("value");
                measurements.add(new Measurement(id, metricID, time, value));
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }      
        return measurements;
    }
    
    /// inner classes
    /**
     * Host class represent Host table in data base.
     */
    class Host {
    	public int id;
    	public String ip;
    	public String hostName;
    	Host(int id, String hostName, String ip){
    		this.id = id;
    		this.ip = ip;
    		this.hostName = hostName;
    	}
    }
    
    /**
     * Metric class represent Metric table in data base.
     */
    class Sensor {
    	public int id;
    	public String sensorName;
    	public int hostID;
    	
    	Sensor(int id, String sensorName, int hostID){
    		this.id = id; 		
    		this.sensorName = sensorName;
    		this.hostID = hostID;
    	}
    }
    
//  /**
//  * Metric class represent Metric table in data base.
//  */
// class Metric {
// 	public int id;
// 	public int sensorID;
// 	public String metricName;
// 	Metric(int id, int sensorID, String metricName){
// 		this.id = id;
// 		this.sensorID = sensorID;
// 		this.metricName = metricName;
// 	}
// }
    
    /**
     * Metric class represent Metric table in data base.
     */
    class Metric {
    	public int id;
    	public int sensorID;
    	public String metricName;
    	Metric(int id, int sensorID, String metricName){
    		this.id = id;
    		this.sensorID = sensorID;
    		this.metricName = metricName;
    	}
    }
    
    /**
     * Measurement class represent Measurement table in data base.
     */
    class Measurement {
    	public int id;
    	public int metricID;
    	public String time;
    	public double value;
    	Measurement(int id, int metricID, String time, double value){
    		this.id = id;
    		this.metricID = metricID;
    		this.time = time;
    		this.value = value;
    	}
    }
}
