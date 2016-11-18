package cs601.database;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs601.entity.Address;
import cs601.entity.Hotel;
import cs601.entity.Review;
import cs601.entity.ThreadSafeHotelData;
import cs601.enumeration.Status;






/**
 * Handles all database-related actions. Uses singleton design pattern. Modified
 * by Prof. Karpenko from the original example of Prof. Engle.
 * 
 * @see RegisterServer
 */
public class DatabaseHandler {

	
	
	/** Makes sure only one database handler is instantiated. */
	private static DatabaseHandler singleton = new DatabaseHandler();

	
	
	/** Used to determine if login_users table exists. */
	private static final String TABLE_LOGIN_USER = "SHOW TABLES LIKE 'login_users';";
	
	
	private static final String TABLE_HOTELS = "SHOW TABLES LIKE 'hotels';";
	
	
	private static final String TABLE_REVIEWS = "SHOW TABLES LIKE 'reviews';";

	
	
	
	/** Used to create login_users table for this example. */
	private static final String CREATE_LOGIN_USER = "CREATE TABLE login_users ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " + "username VARCHAR(32) NOT NULL UNIQUE, "
			+ "password CHAR(64) NOT NULL, " + "usersalt CHAR(32) NOT NULL);";
	
	
	
	
	private static final String CREATE_HOTELS = "CREATE TABLE hotels ("
			+ "hotelid VARCHAR(255) PRIMARY KEY, " + "hotelname VARCHAR(255) NOT NULL, "
			+ "hoteladdr VARCHAR(255) NOT NULL, " + "hotelcountry CHAR(255) NOT NULL, lat DOUBLE, lon DOUBLE);";
	
	
	
	private static final String CREATE_REVIEWS = "CREATE TABLE reviews ("
			+ "reviewid VARCHAR(255) PRIMARY KEY, " + "hotelid VARCHAR(255) NOT NULL, "
			+ "title CHAR(255), " + "text TEXT(2550), " + "rating INT, " + "userid INT);";

	
	
	/** Used to insert a new user's info into the login_users table */
	private static final String INSERT_USER = "INSERT INTO login_users (username, password, usersalt) "
			+ "VALUES (?, ?, ?);";
	
	
	
	
	private static final String INSERT_HOTEL = "INSERT INTO hotels (hotelid, hotelname, hoteladdr, hotelcountry, lat, lon) "
			+ "VALUES (?, ?, ?, ?, ?, ?);";

	
	
	
	private static final String INSERT_REVIEW = "INSERT INTO reviews (reviewid, hotelid, title, text, rating, userid) "
			+ "VALUES (?, ?, ?, ?, ?, ?);";
	
	
	
	private static final String GET_ONE_REVIEW = "SELECT title, text, rating FROM reviews where reviewid= ? ";  
	
	
	
	private static final String DELETE_ONE_REVIEW = "DELETE FROM reviews WHERE reviewid = ?";
	
	
	
	/** Used to determine if a username already exists. */
	private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";

	// ------------------ constants below will be useful for the login operation
	// once you implement it
	
	
	private static final String USERID_BY_NAME = "SELECT userid FROM login_users WHERE username = ?";
	
	
	private static final String REVIEW_BY_USERID = "SELECT reviewid, hotelid, title, text, rating FROM reviews WHERE userid = ?";
	
	
	private static final String HOTELID_BY_REVIEWID = "SELECT hotelid FROM reviews WHERE reviewid = ?";
	
	
	private static final String HOTEL_BY_ID = "SELECT hotelname, hoteladdr, hotelcountry, lat, lon FROM hotels WHERE hotelid = ?";
	
	
	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";
	
	
	
	private static final String USERID_SQL = "SELECT userid FROM login_users WHERE username = ?";

	
	
	private static final String PASS_SQL = "SELECT password FROM login_users " + "WHERE username = ? AND usersalt = ?";
	
	
	
	/** Used to authenticate a user. */
	private static final String AUTH_SQL = "SELECT username FROM login_users " + "WHERE username = ? AND password = ?";

	
	
	
	
	/** Used to remove a user from the database. */
	private static final String DELETE_SQL = "DELETE FROM login_users WHERE username = ?";

	
	
	
	private static final String ALL_HOTEL = "SELECT * FROM hotels;";
	
	
	
	
	private static final String ALL_REVIEW = "SELECT * FROM reviews WHERE hotelid = ?";
	
	
	
	private static final String GET_USERID = "SELECT userid FROM login_users WHERE username= ?";
	
	
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;

	
	
	
	
	/** Used to generate password hash salt for user. */
	private Random random;

	

	
	
	private ThreadSafeHotelData hdata = null;
	private Set<String> hotelsSet = null;
	private Set<String> reviewsSet = null;
	
	
	
	
	
	
	
	public void addDataToDb(ThreadSafeHotelData hdata){
		this.hdata = hdata;
		DatabaseHandler dh = new DatabaseHandler();
//		dh.addUsers(hdata);
//		dh.addHotels(hdata);
//		dh.addReviews(hdata);
	}
	
	
	
	
	
	
	
	
	public void addUsers(ThreadSafeHotelData hdata){
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			
			Iterator iter1 = hdata.getReviewIter();
			Set<String> usernameSet = new HashSet<String>();
			while(iter1.hasNext()){
				Set set = (Set)iter1.next();
				for(Object obj : set){
					Review review = (Review) obj;
					String username = review.getUsername();
					if(username == null || username.length() == 0){
						username = "anonymous";
					}
					username = username.trim().toLowerCase(); 
					if(!usernameSet.add(username)){
						continue;
					}					
					String password = "123Aa@";
					byte[] saltBytes = new byte[16];
					random.nextBytes(saltBytes);

					String usersalt = encodeHex(saltBytes, 32); // hash salt
					String passhash = getHash(password, usersalt); // combine
																	// password and
																	// salt and hash
																	// again
					// add user info to the database table
					try (PreparedStatement statement = connection.prepareStatement(INSERT_USER);) {
						statement.setString(1, username);
						statement.setString(2, passhash);
						statement.setString(3, usersalt);
						statement.executeUpdate();
					}
				}
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}		
	}
	
	
	
	
	
	
	
	
	
	public void addHotels(ThreadSafeHotelData hdata){
		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			
			Iterator iter = hdata.getHotelIter();
			hotelsSet = new HashSet<String>();
			
			while(iter.hasNext()){
				Hotel hotel = (Hotel)iter.next();
				String hotelid = hotel.getHotelId();
				String hotelname = hotel.getHotelName();
				String hoteladdr = hotel.getAddress().toString();
				String hotelcountry = hotel.getCountry();
				double lat = hotel.getLatitude();
				double lon = hotel.getLongtitude();
				if(hotelsSet.contains(hotelid)){
					continue;
				}
				hotelsSet.add(hotelid);
				
				try (PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL);) {
					statement.setString(1, hotelid);
					statement.setString(2, hotelname);
					statement.setString(3, hoteladdr);
					statement.setString(4, hotelcountry);
					statement.setDouble(5, lat);
					statement.setDouble(6, lon);
					statement.executeUpdate();
				}
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}		
	}
	
	
	
	
	
	
	
	
	
	public void addReviews(ThreadSafeHotelData hdata){
		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			
			Iterator iter1 = hdata.getReviewIter();
			reviewsSet = new HashSet<String>();
			while(iter1.hasNext()){
				Set set = (Set)iter1.next();
				for(Object obj : set){
					Review review = (Review) obj;
					String reviewid = review.getReviewId();
					String hotelid = review.getHotelId();
					String title = review.getReviewTitle();
					String text = review.getReviewText();
					int rating = review.getRating();
					String username = review.getUsername().trim().toLowerCase();
					if(username == null || username.length() == 0){
						username = "anonymous";
					}
					if(reviewsSet.contains(reviewid)){
						continue;
					}
					reviewsSet.add(reviewid);
//					System.out.println("username is: " + username);
					try (PreparedStatement statement1 = connection.prepareStatement(GET_USERID);) {
						statement1.setString(1, username);
						ResultSet result = statement1.executeQuery();
						if(result.next()){
//							System.out.println(result.getInt("userid"));
							int userid = result.getInt("userid");
//							String userid = getUserId(connection, username);
							
							try (PreparedStatement statement2 = connection.prepareStatement(INSERT_REVIEW);) {
								statement2.setString(1, reviewid);
								statement2.setString(2, hotelid);
								statement2.setString(3, title);
								statement2.setString(4, text);
								statement2.setInt(5, rating);
								statement2.setInt(6, userid);
								statement2.executeUpdate();
							}
						}
					}
				}
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}		
	}
	
	
	
	
	
	
	
	
	
	/**
	 * This class is a singleton, so the constructor is private. Other classes
	 * need to call getInstance()
	 */
	private DatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {
			db = new DatabaseConnector("database.properties");
			if(db.testConnection()){
				if(	setupTables(TABLE_LOGIN_USER, CREATE_LOGIN_USER) != Status.OK || 
					setupTables(TABLE_HOTELS, CREATE_HOTELS) != Status.OK ||
					setupTables(TABLE_REVIEWS, CREATE_REVIEWS) != Status.OK){
					status = Status.CREATE_FAILED;
				}
			}
			else{
				status = Status.CONNECTION_FAILED;
			}
		} catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		} catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			System.out.println("Error while obtaining a connection to the database: " + status);
		}
	}

	
	
	
	
	
	
	
	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static DatabaseHandler getInstance() {
		return singleton;
	}

	
	
	
	
	
	
	
	
	/**
	 * Checks to see if a String is null or empty.
	 * 
	 * @param text
	 *            - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	
	
	
	
	
	
	
	/**
	 * 
	 * password matching
	 * @param pw
	 * @return
	 */
	public static boolean validPassWord(String pw){
		System.out.println(pw);
		if(pw.matches("(.*)[0-9]+(.*)") && pw.matches("(.*)[A-Z]+(.*)") && pw.matches("(.*)[a-z]+(.*)") && pw.matches("(.*)[@#$%^&\\+=]+(.*)") && pw.length() >= 6 && pw.length() <= 20){
			System.out.println("password is valid");
			return true;
		}
		return false;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables(String checkState, String createState) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(checkState).next()) {
				// Table missing, must create
				statement.executeUpdate(createState);

				// Check if create was successful
				if (!statement.executeQuery(checkState).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
		}

		return status;
	}

	
	
	
	
	
	
	
	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		} catch (SQLException e) {
			status = Status.SQL_EXCEPTION;
			System.out.println("Exception occured while processing SQL statement:" + e);
		}

		return status;
	}

	
	
	
	
	
	
	
	
	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            - byte array to encode
	 * @param length
	 *            - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	
	
	
	
	
	
	
	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            - password to hash
	 * @param salt
	 *            - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		} catch (Exception ex) {
			System.out.println("Unable to properly hash password." + ex);
		}

		return hashed;
	}

	
	
	
	
	
	
	
	
	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		System.out.println("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_REGISTRATION;
			System.out.println("Invalid regiser info");
			return status;
		}
		
		// password does not satisfied
		if(!validPassWord(newpass)){
			System.out.println("password not meet requirement");
			status = Status.INVALID_PASSWORD;
			return status;
		}

		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				// generate salt
				byte[] saltBytes = new byte[16];
				random.nextBytes(saltBytes);

				String usersalt = encodeHex(saltBytes, 32); // hash salt
				String passhash = getHash(newpass, usersalt); // combine
																// password and
																// salt and hash
																// again

				// add user info to the database table
				try (PreparedStatement statement = connection.prepareStatement(INSERT_USER);) {
					statement.setString(1, newuser);
					statement.setString(2, passhash);
					statement.setString(3, usersalt);
					statement.executeUpdate();

					status = Status.OK;
				}
			}
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.out.println("Error while connecting to the database: " + ex);
		}

		return status;
	}

	
	
	
	
	
	
	
	
	/**
	 * check for the newuser, newpassword and salt infomation in the database.
	 * @param newuser
	 * @param newpass
	 * @return
	 */
	public Status loginUser(String user, String pass) {
		Status status = Status.ERROR;
		System.out.println("Login " + user + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(user) || isBlank(pass)) {
			status = Status.INVALID_LOGIN;
			System.out.println("Invalid login info");
			return status;
		}
		
		// password does not satisfied
		if(!validPassWord(pass)){
			System.out.println("password not meet requirement");
			status = Status.INVALID_PASSWORD;
			return status;
		}

		// try to connect to database and test for password of user
		try (Connection connection = db.getConnection();) {

			assert connection != null;
			assert user != null;

			try (PreparedStatement statement1 = connection.prepareStatement(USER_SQL);) {
				statement1.setString(1, user);
				ResultSet result1 = statement1.executeQuery();
				if(result1.next()){
					String salt = getSalt(connection, user);
					String passhash = getHash(pass, salt);
					String dbpw = getPassWord(connection, user, salt);
					if(passhash.equals(dbpw)){
						System.out.println("User authentificated!");
						status = Status.OK;
					}
					else{
						status = Status.WRONG_PASS;
					}
				}
				else{
					status = Status.INVALID_USER;
				}
				
			} catch (SQLException e) {
				status = Status.SQL_EXCEPTION;
				System.out.println("Exception occured while processing SQL statement:" + e);
			}
			
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.out.println("Error while connecting to the database: " + ex);
		}

		return status;
	}

	
	
	
	
	
	
	
	
	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}
	
	
	
	
	
	
	
	
	
	private String getPassWord(Connection connection, String user, String salt) throws SQLException{
		
		assert connection != null;
		assert user != null;
		assert salt != null;
		
		String password = null;
		
		try (PreparedStatement statement = connection.prepareStatement(PASS_SQL);) {
			statement.setString(1, user);
			statement.setString(2, salt);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				password = results.getString("password");
			}
		}
		return password;
	}
	
	
	
	
	
	
	
	@SuppressWarnings("null")
	public List<Hotel> getHotels(){
		List<Hotel> hotels = new ArrayList<Hotel>();
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(ALL_HOTEL);) {				
				ResultSet result = statement.executeQuery();
				while(result.next()) { 
					 String hotelid = result.getString("hotelid");
					 String hotelname = result.getString("hotelname");
					 String hoteladdr = result.getString("hoteladdr");
					 String hotelcountry = result.getString("hotelcountry");
					 Hotel hotel = new Hotel(hotelid, hotelname, hoteladdr, hotelcountry);
					 hotels.add(hotel);
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hotels;
	}
	
	
	
	
	
	
	
	public List<Review> getReviews(String hotelid){
		List<Review> reviews = new ArrayList<Review>();
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(ALL_REVIEW);) {	
				statement.setString(1, hotelid);
				ResultSet result = statement.executeQuery();
				while(result.next()) { 
					 String reviewid = result.getString("reviewid");
					 String title = result.getString("title");
					 String text = result.getString("text");
					 int rating = result.getInt("rating");
					 String hotelId = result.getString("hotelid");
					 Review review = new Review(reviewid, hotelId, title, text, rating);
					 reviews.add(review);
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reviews;
	}
	
	
	
	
	
	
	
	
	public String getUserIdByName(String username){
		String userId = null;
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(USERID_BY_NAME);) {	
				statement.setString(1, username);
				ResultSet result = statement.executeQuery();
				if(result.next()) { 
					userId = result.getString("userid");
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userId;
	}
	
	
	
	
	
	
	
	
	
	public Hotel getHotelById(String hotelId){
		Hotel hotel = null;
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(HOTEL_BY_ID);) {	
				statement.setString(1, hotelId);
				ResultSet result = statement.executeQuery();
				if(result.next()) { 
					String hotelName = result.getString("hotelname");
					String hotelAddr = result.getString("hoteladdr");
					String hotelCountry = result.getString("hotelcountry");
					double lat = result.getDouble("lat");
					double lon = result.getDouble("lon");
					hotel = new Hotel(hotelId, hotelName, hotelAddr, hotelCountry, lat, lon);
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hotel;
	}
	
	
	
	
	
	
	
	
	public List<Review> getReviewByUserId(String userId){
		List<Review> reviews = new ArrayList<Review>();
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(REVIEW_BY_USERID);) {	
//				System.out.println("Userid is: " + userId);
				statement.setString(1, userId);
				ResultSet result = statement.executeQuery();
				while(result.next()) { 
					String reviewId = result.getString("reviewid");
					String hotelId = result.getString("hotelid");
					String title = result.getString("title");
					String text = result.getString("text");
					int rating = result.getInt("rating");
					Review review = new Review(reviewId, hotelId, title, text, rating);
					reviews.add(review);
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reviews;
	}
	
	
	
	
	
	
	
	
	public String getHotelIdByReviewId(String reviewId){
		String hotelId = null;
		try (Connection connection = db.getConnection();) {
			try (PreparedStatement statement = connection.prepareStatement(HOTELID_BY_REVIEWID);) {	
				statement.setString(1, reviewId);
				ResultSet result = statement.executeQuery();
				if(result.next()) { 
					hotelId = result.getString("hotelid");
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hotelId;
	}
	
	
	
	
	
	
	
	
	
	public void addOneReview(String reviewId, String hotelId, String userId, String title, String text, int rating){
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			try (PreparedStatement statement = connection.prepareStatement(INSERT_REVIEW);) {
				int userIdInt = Integer.parseInt(userId);
				statement.setString(1, reviewId);
				statement.setString(2, hotelId);
				statement.setString(3, title);
				statement.setString(4, text);
				statement.setInt(5, rating);
				statement.setInt(6, userIdInt);
				statement.executeUpdate();
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}		
	}
	
	
	
	
	
	
	
	
	public Review getOneReview(String reviewId, String hotelId){
		Review review = null;
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			try (PreparedStatement statement = connection.prepareStatement(GET_ONE_REVIEW);) {
				statement.setString(1, reviewId);
				ResultSet result = statement.executeQuery();
				if(result.next()){
					String title = result.getString("title");
					String text = result.getString("text");
					int rating = result.getInt("rating");
					review = new Review(reviewId, hotelId, title, text, rating);
				}
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}		
		return review;
	}
	
	
	
	
	
	
	
	
	public void deleteOneReview(String reviewId){
		try (Connection connection = db.getConnection();) {
			// add user info to the database table
			try (PreparedStatement statement = connection.prepareStatement(DELETE_ONE_REVIEW);) {
				statement.setString(1, reviewId);
				statement.executeUpdate();
			}
			
		} catch (SQLException ex) {
			System.out.println("Error while connecting to the database: " + ex);
		}	
	}
	
}




