package cs601.entity;

public class Hotel implements Comparable<Hotel>{
	
	private String hotelId;
	private String hotelName;
	private Address address;
	private String address_string;
	private String country;
	private double latitude;
	private double longitude; 
	
	
	// constructor for hotel
	public Hotel(String hotelId, String hotelName, Address address, String country){
		this.hotelId = hotelId;
		this.hotelName = hotelName;
		this.address = address;
		this.country = country;
	}
	
	public Hotel(String hotelId, String hotelName, String address, String country){
		this.hotelId = hotelId;
		this.hotelName = hotelName;
		address_string = address;
		this.country = country;
	}
	
	public Hotel(String hotelId, String hotelName, String address, String country, double latitude, double longitude){
		this.hotelId = hotelId;
		this.hotelName = hotelName;
		address_string = address;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	
	public Hotel(String hotelId, String hotelName, Address address, String country, double latitude, double longitude){
		this.hotelId = hotelId;
		this.hotelName = hotelName;
		this.address = address;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	//setters and getters
	public String getHotelId() {
		return hotelId;
	}

	public String getHotelName() {
		return hotelName;
	}

	public Address getAddress() {
		return address;
	}
	
	public String getCountry(){
		return country;
	}
	
	public String getAddress_string(){
		return address_string;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongtitude(){
		return longitude;
	}


	@Override 
	public int compareTo(Hotel that){
		return hotelName.compareTo(that.getHotelName());
	}
	
	@Override
	public String toString(){
		return hotelName + ": " + hotelId + "\n" + this.getAddress().toString();  
	}
	
}
