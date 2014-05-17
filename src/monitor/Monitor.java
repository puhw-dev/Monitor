package monitor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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
	private String catalogURL = "http://MaciekSiczekCatalog:80/catalog";
	private RestServer restServer;
	
	/// contructor
	public Monitor() throws IOException{
		restServer = new RestServer();
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
}
