package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.concurrent.WorkQueue;
import cs601.database.DatabaseHandler;
import cs601.entity.Hotel;
import cs601.entity.HotelDataBuilder;
import cs601.entity.Review;
import cs601.entity.ThreadSafeHotelData;
import cs601.entity.TouristAttraction;






@SuppressWarnings("serial")
public class AttractionServlet extends BaseServlet{
	
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Entering attraction page", response);
		System.out.println("Tourist attractions...");
		PrintWriter out = response.getWriter();
		
		toHotelInfoButton(out);
		String hotelId = request.getParameter("hotelId");
		String radius = request.getParameter("radius");
		Hotel hotel = dbhandler.getHotelById(hotelId);
		
		if(hotel != null){
			System.out.println("hotel is: " + hotel.getAddress_string());
			String[] strings = hotel.getAddress_string().split("\n");
			System.out.println("hotel street: " + strings[0]);
			System.out.println("hotel city: " + strings[1]);
		}
		
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}
		
		int radiusInt = Integer.parseInt(radius);
		ThreadSafeHotelData data = new ThreadSafeHotelData();
		HotelDataBuilder builder = new HotelDataBuilder(data);
		List<TouristAttraction> attractions = builder.fetchAttractions(hotel, radiusInt);
		displayHotelTable(out, hotelId, attractions); 
		finishResponse(response);
	}
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Go to search tourist sites", response);
		String btn = request.getParameter("btn");
		
		if(btn != null){
			if(btn.equals("HotelInfo")){
				String url = "/hotelInfo";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	private void displayHotelTable(PrintWriter out, String hotelId, List<TouristAttraction> attractions){
		List<Hotel> hotels = dbhandler.getHotels();
		if(hotels != null){
			out.println("<table>");
			out.println("\t<tr>");
			out.println("\t\t<th>Name</th>");
			out.println("\t\t<th>Address</th>");
			out.println("\t\t<th>Rating</th>");
			out.println("\t</tr>");
			for(int i = 0; i < attractions.size(); i++){
				TouristAttraction attraction = attractions.get(i);
				String name = attraction.getName();
				String address = attraction.getAddress();
				int rating = attraction.getRating();
				
				out.println("\t<tr>");
				out.println("\t\t<td>" + name + "</td>");
				out.println("\t\t<td>" + address + "</td>");
				out.println("\t\t<td>" + rating + "</td>");
				out.println("\t</tr>");
			}
			out.println("</table>");
		}
	}
	
	
	
	
	
	
	
	private void toHotelInfoButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/invalidradius\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
		out.println("</form>");
	}
	
}





