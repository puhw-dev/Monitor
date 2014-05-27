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
    	String createUser = "CREATE TABLE IF NOT EXISTS user ("
    			+ "login text NOT NULL UNIQUE, password text UNIQUE, PRIMARY KEY (login) )";
        String createHost = "CREATE TABLE IF NOT EXISTS host ("
        		+ "id integer NOT NULL PRIMARY KEY AUTOINCREMENT, hostname text UNIQUE,	ip text)";
        String createSensor = "CREATE TABLE IF NOT EXISTS sensor ("
        		+ "id integer NOT NULL PRIMARY KEY AUTOINCREMENT, hostid integer NOT NULL, owner text NOT NULL UNIQUE,"
        		+ "sensorname text,	sensortype text, rpm integer, FOREIGN KEY (hostid) REFERENCES HOST (id),"
        		+ "FOREIGN KEY (owner) REFERENCES USER (login) )";
        String createMetric = "CREATE TABLE IF NOT EXISTS metric ("
        		+ "id integer NOT NULL PRIMARY KEY AUTOINCREMENT, name text)";     
        String createMeasurement = "CREATE TABLE IF NOT EXISTS measurement ("
        		+ "id integer NOT NULL PRIMARY KEY AUTOINCREMENT, metricid integer NOT NULL, sensorid integer NOT NULL,"
        		+ "time text, value text, FOREIGN KEY (metricid) REFERENCES METRIC (id),"
        		+ "FOREIGN KEY (sensorid)	REFERENCES SENSOR (id) )";
        try {
            Statement stat = conn.createStatement();
            stat.execute(createUser);
            stat.execute(createHost);
            stat.execute(createSensor);
            stat.execute(createMetric);
            stat.execute(createMeasurement);
        } catch (SQLException e) {
            System.err.println("Error in creating tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    /// public api
    /**
     * Add new user
     * @param user to add
     */
    public void addUser(User user){
    	try {
        	Statement stat = conn.createStatement();
        	stat.executeUpdate("INSERT INTO User (login,password) VALUES ('"+user.login+"','"+user.password+"')");
            stat.close();
        } catch (SQLException e) {
        	System.err.println("add User err: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get list of available users.
     * @return users list.
     */
    public List<User> getUsers(){
    	List<User> users = new LinkedList<User>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM user");
            String login, password;
            while(result.next()) {
                login = result.getString("login");
                password = result.getString("password");            
                users.add(new User(login, password));
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }     
        return users;
    }
    
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
     * Get sensors list of specific host
     * @param hostID host id
     * @return sensor list
     */
    public List<Sensor> getSensors(int hostID){
    	List<Sensor> sensors = new LinkedList<Sensor>();
        try {
        	Statement stat = conn.createStatement();
            ResultSet result = stat.executeQuery("SELECT * FROM sensor where hostID="+hostID);
            int id;
            String owner, sensorName, sensorType;
            int rpm; 
            while(result.next()) {
                id = result.getInt("id");
                owner = result.getString("owner");
                sensorName = result.getString("sensorname");
                sensorType = result.getString("sensortype");
                rpm = result.getInt("rpm");
                sensors.add(new Sensor(id, hostID, owner, sensorName, sensorType, rpm));
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
         		sensor = new Sensor(result.getInt("id"), result.getInt("hostid"), result.getString("owner"),
         				result.getString("sensorName"), result.getString("sensorType"), result.getInt("rpm"));
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
            ResultSet result = stat.executeQuery("SELECT * FROM metric m where m.id in (select metricid from measurement "
            		+ "where sensorID="+sensorID+")");
            int id;
            String name;
            while(result.next()) {
                id = result.getInt("id");
                name = result.getString("name");
                metrics.add(new Metric(id, name));
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
        	ResultSet result = stat.executeQuery("SELECT * FROM metric m where m.name='"+metricName+"' and m.id in (select metricid from measurement "
            		+ "where sensorID="+sensorID+")");
        	if(result.next()){
         		metric = new Metric(result.getInt("id"), result.getString("name"));
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
     * User class represent User table in data base
     */
    class User {
    	public String login;
    	public String password;
		public User(String login, String password) {
			this.login = login;
			this.password = password;
		}
    }
    
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
    	public int hostID;
    	public String owner;
    	public String sensorName;
    	public String sensorType;
    	public int rpm;
		public Sensor(int id, int hostID, String owner, String sensorName, String sensorType, int rpm) {
			this.id = id;
			this.hostID = hostID;
			this.owner = owner;
			this.sensorName = sensorName;
			this.sensorType = sensorType;
			this.rpm = rpm;
		}
    }
        
    /**
     * Metric class represent Metric table in data base.
     */
    class Metric {
    	public int id;
    	public String metricName;
    	Metric(int id, String metricName){
    		this.id = id;
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
