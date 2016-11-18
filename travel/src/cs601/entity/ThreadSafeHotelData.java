package cs601.entity;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cs601.concurrent.ReentrantReadWriteLock;
import cs601.entity.Hotel;




/**
 * Class HotelData - a data structure that stores information about hotels and
 * hotel reviews. Allows to quickly lookup a hotel given the hotel id. 
 * Allows to easily find hotel reviews for a given hotel, given the hotelID. 
 * Reviews for a given hotel id are sorted by the date and user nickname.
 *
 */

public class ThreadSafeHotelData {


	
	
	// - declare data structures to store hotel data
	private final Map<String, Hotel> hotelMap;
	private final Map<String, Set<Review>> reviewMap;
	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static final DateFormat newFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.US);
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	
	
	
	/**
	 * get the review map
	 * @return
	 */
	public Map<String, Set<Review>> getReviewMap(){
		return reviewMap;
	}
	
	
	
	
	
	
	/**
	 * Default constructor.
	 */
	
	public ThreadSafeHotelData() {
		// Initialize all data structures
		
		hotelMap = new TreeMap<>();
		reviewMap = new HashMap<>();
		
	}
	
	


	
	
	
	@SuppressWarnings("rawtypes")
	public Iterator getHotelIter(){
		Iterator iter = hotelMap.values().iterator();
		return iter;
	}
	
	
	
	
	
	@SuppressWarnings("rawtypes")
	public Iterator getReviewIter(){
		Iterator iter = reviewMap.values().iterator();
		return iter;
	}
	
	
	
	
	
	
	
	/**
	 * Create a Hotel given the parameters, and add it to the appropriate data
	 * structure(s).
	 * 
	 * @param hotelId
	 *            - the id of the hotel
	 * @param hotelName
	 *            - the name of the hotel
	 * @param city
	 *            - the city where the hotel is located
	 * @param state
	 *            - the state where the hotel is located.
	 * @param streetAddress
	 *            - the building number and the street
	 * @param latitude
	 * @param longitude
	 * @return 
	 */
	
	
	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
			double lon, String country) {
		lock.lockWrite();
		try{
			if(hotelMap.get(hotelId) == null){
				Address address = new Address(streetAddress, city, state, lat, lon);
				Hotel hotel = new Hotel(hotelId, hotelName, address, country, lat, lon);
				
				//put the hotel information into the hotelMap.
				hotelMap.put(hotelId, hotel);
			}
		}
		finally{
			lock.unlockWrite();
		}
	}
	


	
	 
	
	
	
	
	
	/**
	 * Add a new review.
	 * 
	 * @param hotelId
	 *            - the id of the hotel reviewed
	 * @param reviewId
	 *            - the id of the review
	 * @param rating
	 *            - integer rating 1-5.
	 * @param reviewTitle
	 *            - the title of the review
	 * @param review
	 *            - text of the review
	 * @param isRecommended
	 *            - whether the user recommends it or not
	 * @param date
	 *            - date of the review in the format yyyy-MM-dd, e.g.
	 *            2016-08-29.
	 * @param username
	 *            - the nickname of the user writing the review.
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle ParseException if the date is invalid.
	 *         Needs to check whether the rating is in the correct range
	 */
	
	
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
			boolean isRecom, String date, String username) {
		// FILL IN CODE
		
		lock.lockWrite();
		try{
			Set<Review> reviewSet;
 			if(reviewMap.get(hotelId) == null){
 				reviewSet = new TreeSet<Review>();
			}
 			else{
 				reviewSet = reviewMap.get(hotelId);
 			}
			if(rating <= 0 || rating >= 6 || !isValidDate(date)){
				return false;
			}
			Review r = new Review(reviewId, hotelId, reviewTitle, review, isRecom, username, date, rating);
			reviewSet.add(r);
			
			reviewMap.put(hotelId, reviewSet);
			return true; 
		}
		finally{
			lock.unlockWrite();
		}
	}
	


	
	
	
	
	
	/**
	 * check if a date is valid or not
	 * @param date
	 * @return
	 */
	private boolean isValidDate(String date) {
	    Date d = null;
	    try {
			d = format.parse(date);			
			return d != null;
		} catch (java.text.ParseException e) {
			return false;
		}
	}

	
	
	
	
	
	
	/**
	 * Return an alphabetized list of the ids of all hotels
	 * @return
	 */
	
	public List<String> getHotels() {
		// FILL IN CODE
		lock.lockRead();
		try{
			List<String> hotelIds = new ArrayList<>();
			if(hotelIds != null){
				hotelIds = new ArrayList<String>(hotelMap.keySet());
			}
			return hotelIds;
		}
		finally{
			lock.unlockRead();
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Returns a string representing information about the hotel with the given
	 * id, including all the reviews for this hotel separated by
	 * -------------------- Format of the string: HoteName: hotelId
	 * streetAddress city, state -------------------- Review by username: rating
	 * ReviewTitle ReviewText -------------------- Review by username: rating
	 * ReviewTitle ReviewText ...
	 * 
	 * @param hotel
	 *            id
	 * @return - output string.
	 */
	
	public String toString(String hotelId) {
		lock.lockRead();
		try{
			Hotel hotel = hotelMap.get(hotelId);
			if(hotel != null){
				// get review list from reviewMap by hotelId
				Set<Review> reviewSet = reviewMap.get(hotelId);
				StringBuffer reviews = new StringBuffer();
				reviews.append(hotel.toString());
				if(reviewSet != null){
					//make for loop for the review list and store the information into reviews.
					for(Review review : reviewSet){
						reviews.append("--------------------").append("\n").append(review.toString());
					}
				}
				return reviews.toString();
			}
			return ""; 
		}
		finally{
			lock.unlockRead();
		}
	}

	
	
	
	
	
	
	
	
	/**
	 * Save the string representation of the hotel data to the file specified by
	 * filename in the following format: 
	 * an empty line 
	 * A line of 20 asterisks ******************** on the next line 
	 * information for each hotel, printed in the format described in the toString method of this class.
	 * 
	 * @param filename
	 *            - Path specifying where to save the output.
	 */
	
	public void printToFile(Path filename) {
		
		lock.lockRead();
		try{
//			 create a new file if it is not existed; 
//			 otherwise delete previous one and create a new one.
			if(!Files.exists(filename)){
				try {
					Files.createFile(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				try {
					Files.delete(filename);
					Files.createFile(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			for(String hotelId : hotelMap.keySet()){
				String output = this.toString(hotelId);
							
				// start of the hotel.
				String emptyLine = "\n";
				String ast = "********************";
				
				try {
					Files.write(filename, emptyLine.getBytes(), StandardOpenOption.APPEND);
					Files.write(filename, ast.getBytes(), StandardOpenOption.APPEND);
					Files.write(filename, emptyLine.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try{
					Files.write(filename, output.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		finally{
			lock.unlockRead();
		}
			
	}
	
	
	
	
	
	
	
	
	
	/**
	 * merge a singleHotelData into the review Map.
	 */
	public void combineHotelData(ThreadSafeHotelData singleHotelData){
		lock.lockWrite();
		try{
			reviewMap.putAll(singleHotelData.getReviewMap());
		}
		finally{
			lock.unlockWrite();
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * construct the JsonObject that is about to give to response
	 */
	public JSONObject constructValidHotelJson(String hotelId){
		
		Hotel hotel = hotelMap.get(hotelId);
		Map<String, Object> obj = new LinkedHashMap<>();
		
		obj.put("success",true);
		obj.put("hotelId",hotelId);
		obj.put("name", hotel.getHotelName());
		obj.put("addr", hotel.getAddress().getStreetAddr());
		obj.put("city", hotel.getAddress().getCity());
		obj.put("state", hotel.getAddress().getState());
		obj.put("country", hotel.getCountry());
		JSONObject jsonResponse = new JSONObject(obj);
		return jsonResponse;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject constructValidReviewJson(String hotelId, int num){
		
		Set<Review> reviews = reviewMap.get(hotelId);
		num = num > reviews.size() ? reviews.size() : num;	
		
		JSONObject jsonResponse = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonResponse.put("success",true);
		jsonResponse.put("hotelId",hotelId);
		
		for(Review review : reviews){
			
			num--;
			if(num < 0){
				break;
			}
			else{
				JSONObject subObject = new JSONObject();
				subObject.put("reviewId", review.getReviewId());
				subObject.put("title", review.getReviewTitle());
				subObject.put("user", review.getUsername());
				subObject.put("reviewText", review.getReviewText());
				Date date = review.getDate();
				String newDate = newFormat.format(date);
				subObject.put("date", newDate);
				jsonArray.add(subObject);
			}
		}
		
		jsonResponse.put("reviews", jsonArray);		
		return jsonResponse;
	}
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject constructInvalidJson(){
		
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("success", false);
		jsonResponse.put("hotelId", "invalid");
		return jsonResponse;
		
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * get the url string
	 * @param hotel
	 * @param radius
	 * @return
	 */
	String constructUrlString(Hotel hotel, int radius, String apiKey){
		StringBuffer sb = new StringBuffer();
		sb.append("https://maps.googleapis.com/maps/api/place/textsearch/json?query=tourist%20attractions+in+");

		System.out.println("hotel is: " + hotel.getAddress_string());
		String[] strings = hotel.getAddress_string().split("\n");
		
		String city = strings[1].split(",")[0].trim();
		System.out.println("city is: " + city);
		city = city.replaceAll("\\s+", "%20");
		sb.append(city).append("&");
		double lat = hotel.getLatitude();
		double lon = hotel.getLongtitude();
		sb.append("location=").append(lat).append(",").append(lon).append("&radius=").append(radius).append("&key=");
		sb.append(apiKey);
		return sb.toString();
	}
	
	
	

}
