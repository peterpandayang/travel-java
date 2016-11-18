package cs601.server;

import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import cs601.concurrent.WorkQueue;
import cs601.database.DatabaseHandler;
import cs601.entity.HotelDataBuilder;
import cs601.entity.ThreadSafeHotelData;





public class Driver {

	
	private static final int THREADS = 5;
	
	private static ThreadSafeHotelData hdata = new ThreadSafeHotelData();
	private static HotelDataBuilder builder = new HotelDataBuilder(hdata, new WorkQueue(THREADS));
	private static MainServer server = null;
	

	
	
	
	/**
	 * set resource for the two servers
	 */
	public void setResource(){
		
		server = new MainServer(hdata);
		
	}
	
	
	
	
	
	
	public static void main(String[] args){
		
		// load information for hotel
		builder.loadHotelInfo("input/hotels200.json");
		builder.loadReviews(Paths.get("input/reviews"));
		
		DatabaseHandler dh = DatabaseHandler.getInstance();
		dh.addDataToDb(hdata);
		
		Driver driver = new Driver();
		driver.setResource();
		
		try {
			server.start();
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

}
