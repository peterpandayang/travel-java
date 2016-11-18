package cs601.entity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cs601.concurrent.WorkQueue;
import cs601.entity.Hotel;
import cs601.entity.ThreadSafeHotelData;
import cs601.entity.TouristAttraction;
import cs601.service.AttractionServlet;


public class HotelDataBuilder {

//	private static final Logger logger = LogManager.getLogger();
	private static ThreadSafeHotelData hotelData;
	private WorkQueue queue;
	private volatile int numTasks; // how many runnable tasks are pending
	
	
	
	
	
	
	/**
	 * Construct the HotelDataBuilder 
	 */
	public HotelDataBuilder(ThreadSafeHotelData data){
		hotelData = data;
	}
	
	
	
	
	
	
	public HotelDataBuilder(ThreadSafeHotelData data, WorkQueue q){
		hotelData = data;
		queue = q;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * add hotel from json object 
	 * @param obj
	 */
	private void addHotelFromJson(JSONObject obj){
		
		String hotelId = (String) obj.get("id");
		String hotelName = (String) obj.get("f");
		String city = (String) obj.get("ci");
		String state =  (String) obj.get("pr");
		String streetAddress =  (String) obj.get("ad");
		String s1 = (String)(((JSONObject)obj.get("ll")).get("lng"));
		double lon =  Double.parseDouble(s1);
		String s2 = (String) (((JSONObject)obj.get("ll")).get("lat"));
		double lat = Double.parseDouble(s2);
		String country = (String) obj.get("c");
		
		// construct hotel
		hotelData.addHotel(hotelId, hotelName, city, state, streetAddress, lat, lon, country);		
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Read the json file with information about the hotels (id, name, address,
	 * etc) and load it into the appropriate data structure(s). Note: This
	 * method does not load reviews
	 * 
	 * @param filename
	 *            the name of the json file that contains information about the
	 *            hotels
	 */
	
	public void loadHotelInfo(String jsonFilename) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser.parse(new FileReader(jsonFilename));
			JSONArray array = (JSONArray) object.get("sr");
			for(int i = 0; i < array.size(); i++){
				JSONObject obj =(JSONObject)array.get(i);
				addHotelFromJson(obj);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Load reviews for all the hotels into the appropriate data structure(s).
	 * Traverse a given directory recursively to find all the json files with
	 * reviews and load reviews from each json. Note: this method must be
	 * recursive and use DirectoryStream as discussed in class.
	 * 
	 * @param path
	 *            the path to the directory that contains json files with
	 *            reviews Note that the directory can contain json files, as
	 *            well as subfolders (of subfolders etc..) with more json files
	 */
	
	public void loadReviews(Path path) {
		traverseDirectoryContents(path);
		shutdown();
	}
	
	
	
	
	
	
	
	
	
	/**
	 * traverse the json content under give path and add the reviews to the review map
	 * @param path
	 */
	private synchronized void traverseDirectoryContents(Path path){
		try {
			DirectoryStream<Path> fileList = Files.newDirectoryStream(path);
			for(Path entry : fileList){
				if (Files.isDirectory(entry)) {
					traverseDirectoryContents(entry);
		        }
				else{
					String jsonFile = entry.toString();
					queue.execute(new JsonWorker(jsonFile));
				}
			}
		} catch (IOException e) {
//			logger.catching(Level.DEBUG, e);
			e.printStackTrace();
		}
	}		
		
		
		
		
	
	
	
	
	public class JsonWorker implements Runnable{
		private ThreadSafeHotelData singleHotelData;
		private String JsonFile;
		JsonWorker(String file) {
			JsonFile = file;
			singleHotelData = new ThreadSafeHotelData();
			incrementTasks();
		}
		
		@Override
		public void run() {
			try{
				loadReviewsInfo(JsonFile, singleHotelData);
				if(singleHotelData != null && singleHotelData.getReviewMap().size() != 0){
					hotelData.combineHotelData(singleHotelData);
				}
			}
			finally{
				decrementTasks();
			}
		}
	}
	
	
	
	
	
	
	
	
	
	/** Increment the number of tasks */
	public synchronized void incrementTasks() {
		numTasks++;		
	}
	
	
	
	
	
	
	
	
	
	/** Decrement the number of tasks. 
	 * Call notifyAll() if no pending work left.  
	 */
	public synchronized void decrementTasks() {
		numTasks--;
		if (numTasks <= 0) {
			notifyAll();
		}
	}
	
	
	
	
	
	
	
	
	
	/**
	 *  Wait for all pending work to finish
	 */
	private synchronized void waitUntilFinished() {
		while (numTasks > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
//				logger.warn("Got interrupted while waiting for pending work to finish, ", e);
			}
		}
	}
	
	
		
		
	
	
	
	
	
	
	/** Wait until there is no pending work, then shutdown the queue */
	public synchronized void shutdown() {
		waitUntilFinished();
		queue.shutdown();
	}
	
	
	
	
	
	
	
	
		
		
	/**
	 * read a single json file with reviews.
	 * @param jsonFilename
	 */
	private void loadReviewsInfo(String jsonFilename, ThreadSafeHotelData singleHotelData){
		if(jsonFilename.indexOf(".json") < 0){
			return;
		}
		JSONParser parser = new JSONParser();
		
		// get the object from the input json file and cast it to json object
		try {
			JSONObject object = (JSONObject) parser.parse(new FileReader(jsonFilename));
			JSONObject reviewDetails = (JSONObject) object.get("reviewDetails");
			JSONObject reviewSummaryCollection = (JSONObject) reviewDetails.get("reviewSummaryCollection");
			JSONArray reviewSummary = (JSONArray) reviewSummaryCollection.get("reviewSummary");
			JSONObject summarys = (JSONObject) reviewSummary.get(0);
			String hotelId = (String) summarys.get("hotelId");
			JSONObject reviewCollection = (JSONObject) reviewDetails.get("reviewCollection");
			JSONArray reviewArray = (JSONArray) reviewCollection.get("review");
			for(int i = 0; i < reviewArray.size(); i++){
				JSONObject obj = (JSONObject) reviewArray.get(i);
				addReviewFromJson(obj, hotelId, singleHotelData);
			}		
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}		
		
		
		
		
		
		
		
		
		
	/**
	 * check and add review to list for its hotel.
	 * @param obj
	 * @param hotelId
	 */
	private synchronized void addReviewFromJson(JSONObject obj, String hotelId, ThreadSafeHotelData singleHotelData){
		// passing parameters
		String reviewId = (String) obj.get("reviewId");		
		Long longRating = (Long) obj.get("ratingOverall");
		int rating = longRating.intValue();
		String username = (String) obj.get("userNickname");
		if(username == null || username.trim().equals("")){
			username = "anonymous";
		}
		String reviewTitle = (String) obj.get("title");
		if(reviewTitle == null || reviewTitle.equals("")){
			reviewTitle = "";
		}
		String reviewText = (String) obj.get("reviewText");
		if(reviewText == null || reviewText.trim().equals("")){
			reviewText = "";
		}
		boolean isRecom =  obj.get("isRecommended").equals("YES") ? true : false;
		String rawDate = (String) obj.get("reviewSubmissionTime");
		
		singleHotelData.addReview(hotelId, reviewId, rating, reviewTitle, reviewText, isRecom, rawDate, username);
	}	
	
	
	
	
	
	
	/**
	 * call the printToFile method of hotelData.
	 * @param filename
	 */
	public void printToFile(Path filename) {
		hotelData.printToFile(filename);
	}
	
	
	
	
	
	
	
	
	
	private static String getApiKey(String filename){
		StringBuffer api = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = "";
			while((line = br.readLine()) != null){
				api.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return api.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * fetch the attractions for hotels with given radius
	 * @param radiusInMiles
	 */
	public List<TouristAttraction> fetchAttractions(Hotel hotel, int radiusInMiles){
		int numberOfRequest = 1;
		
		int radiusInMeters = (int) (radiusInMiles * 1609);
		String apiKey = getApiKey("input/apiKey");
		if(hotel == null){
			System.out.println("hotel is: null");
		}
		String urlString = hotelData.constructUrlString(hotel, radiusInMeters, apiKey);
		
		String s = "";
		
		List<TouristAttraction> attractions = new ArrayList<TouristAttraction>();
		
		URL url;
		PrintWriter out = null;
		BufferedReader br = null;
		SSLSocket socket = null;
		
		try {
			url = new URL(urlString);
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// create socket
			socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
			// get output 
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String request = getRequest(url.getHost(), url.getPath() + "?" + url.getQuery());
			System.out.println(numberOfRequest + " Request: ");
			System.out.println(request);
//			numberOfRequest++;

			out.println(request); // send a request to the server
			out.flush();
			
			// input stream for the secure socket.
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// use input stream to read server's response
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				Pattern pattern = Pattern.compile("^[A-Z]+");
				Matcher m = pattern.matcher(line);
				if(m.lookingAt()){
					continue;
				}
				sb.append(line);
			}
			s = sb.toString();
			
			JSONParser parser = new JSONParser();
			try {
				Object json = parser.parse(s);
				
				System.out.println(json);
				attractions = getAttractions(json, hotel.getHotelId());
			} catch (ParseException e) {
				e.printStackTrace();
			}				
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				// close the streams and the socket
				out.close();
				br.close();
				socket.close();
			} catch (IOException e) {
				System.out.println("An exception occured while trying to close the streams or the socket: " + e);
			}
		}
		return attractions;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Takes a host and a string containing path/resource/query and creates a
	 * string of the HTTP GET request
	 * 
	 * @param host
	 * @param pathResourceQuery
	 * @return
	 */
	private static String getRequest(String host, String pathResourceQuery) {
		String request = "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator() // GET
																			// request
				+ "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
				+ "Connection: close" + System.lineSeparator() // make sure the server closes the
															   // connection after we fetch one page
				+ System.lineSeparator();
		return request;
	}

	
	
	

	
	
	
	
	
	
	
	/**
	 * load attractions from json file
	 * @param jsonFilename
	 */
	private List<TouristAttraction> getAttractions(Object object, String hotelId){
		JSONObject json = (JSONObject) object;
		JSONArray results = (JSONArray) json.get("results");
		int size = results.size();
		List<TouristAttraction> attractions = new ArrayList<TouristAttraction>();
		for(int i = 0; i < size; i++){
			JSONObject attraction = (JSONObject) results.get(i);
			String attractionId = (String) attraction.get("id");
			String name = (String) attraction.get("name");
			String address = (String) attraction.get("formatted_address");
			double rating = 0.0;
			if(attraction.get("rating") instanceof Double){
				rating = ((Double) attraction.get("rating")).doubleValue();
			}
			else if(attraction.get("rating") instanceof Long){
				rating = ((Long) attraction.get("rating")).doubleValue();
			}
			TouristAttraction newAttraction = new TouristAttraction(attractionId, name, rating, address, hotelId);
			attractions.add(newAttraction);
		}
		return attractions;
	}
	

	
	
}