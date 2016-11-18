package cs601.entity;

public class TouristAttraction {
	
	private String attractionId;
	private String name;
	private String address;
	private double rating;
	private String hotelId;
	
	public TouristAttraction(String attractionId, String name, double rating, String address, String hotelId){
		this.attractionId = attractionId;
		this.name = name;
		this.rating = rating;
		this.address = address;
		this.hotelId = hotelId;
	}
	
	
	public String getName(){
		return name;
	}
	
	public String getAddress(){
		return address;
	}
	
	public int getRating(){
		return (int) rating;
	}
	
	public String toString(){
		return name + "; " + address;
	}
	
}
