package cs601.entity;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class Review implements Comparable<Review>{

	private String reviewId;
	private String hotelId;
	private String reviewTitle;
	private String reviewText;
	private String username;
	private Date date;
	private int rating;
	@SuppressWarnings("unused")
	private boolean isRecom;
	
	// constructor
	public Review(String reviewId, String hotelId, String reviewTitle, String reviewText, boolean isRecom, String username, String date, int rating){
		this.reviewId = reviewId;
		this.hotelId = hotelId;
		this.reviewTitle = reviewTitle;
		this.reviewText = reviewText;
		this.username = username;
		
		// convert string date to Date date
		
		try {
			ThreadSafeHotelData.format.setTimeZone(TimeZone.getTimeZone("PST"));
			this.setDate((Date)ThreadSafeHotelData.format.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		this.rating = rating;
		this.isRecom = isRecom;
	}
	
	
	public Review(String reviewId, String hotelId, String reviewTitle, String reviewText, int rating){
		this.reviewId = reviewId;
		this.reviewTitle = reviewTitle;
		this.reviewText = reviewText;
		this.rating = rating;
		this.hotelId = hotelId;
	}
	
	//setters and getters
	public String getReviewId() {
		return reviewId;
	}

	public String getHotelId() {
		return hotelId;
	}

	public String getUsername() {
		return username;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	
	public String getReviewTitle(){
		return reviewTitle;
	}
	
	public String getReviewText(){
		return reviewText;
	}
	
	public int getRating(){
		return rating;
	}


	@Override
	public int compareTo(Review that){
		
		// if the date for the reviews are the same, compare the review with username.
		if(this.date.compareTo(that.getDate()) == 0){
			
			// if the username is the same, compare with the review id.
			if(this.username.equals(that.getUsername())){
				return this.reviewId.compareTo(that.getReviewId());
			}
			
			// compare the username
			return this.username.compareTo(that.getUsername());
		}
		
		//compare the review date
		return this.date.compareTo(that.getDate());
	}
	
	
	@Override
	public String toString(){
		return "Review by " + username + ": " + rating + "\n" + reviewTitle + "\n" + reviewText + "\n";
	}
	
}
