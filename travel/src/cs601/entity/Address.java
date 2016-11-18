package cs601.entity;

public class Address {
	
	private String streetAddress;
	private String city;
	private String state;
	private double latitude;
	private double longitude;
	
	// constructor
	public Address(String streetAddress, String city, String state, double latitude, double longitude){
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	//setters and getters
	
	@Override
	public String toString(){
		return streetAddress + "\n" + city + ", " + state + "\n";
	}
	
	
	public String getStreetAddr(){
		return streetAddress;
	}
	
	public String getCity(){
		return city;
	}
	
	public String getState(){
		return state;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongtitude(){
		return longitude;
	}

	
	
}
