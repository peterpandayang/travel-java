package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.enumeration.Status;
import cs601.entity.*;






@SuppressWarnings("serial")
public class HotelInfoServlet extends BaseServlet{

	
	
	
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
	
	
	
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Entering hotel info page", response);
		System.out.println("Hotel info...");
		PrintWriter out = response.getWriter();
		
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}
		
		// check if the user is in session
		String username = null;
		Cookie[] cookies = request.getCookies();
		if(cookies !=null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("user")) {
					username = cookie.getValue();
				}
			}
			// still in session
			if(username != null){
				toMyRevieButton(out);
				logoutButton(out);
			}
		}		
		displayHotelTable(out); 
		finishResponse(response);
	}

	
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Go to search tourist sites", response);
		String btn = request.getParameter("btn");
		String hotelId = request.getParameter("hotelid");
		String radius = request.getParameter("radius");
		
		if(btn != null){
			if(btn.equals("Search")){
				if(radius == null || radius.length() == 0){
					response.getWriter().println("enter the radius!");
					String url = "/hotelInfo";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				}
				else{
					int radiusInt = Integer.parseInt(radius);
					if(radiusInt >= 0.5 && radiusInt <= 10){
						String url = "/attractions?hotelId=" + hotelId + "&radius=" + radiusInt;
						url = response.encodeRedirectUrl(url);
						response.sendRedirect(url);
					}
					else{
						String url = "/invalidradius";
						url = response.encodeRedirectUrl(url);
						response.sendRedirect(url);
					}
				}
			} 
			else if(btn.equals("Logout")){
				Cookie loginCookie = null;
		    	Cookie[] cookies = request.getCookies();
		    	if(cookies != null){
			    	for(Cookie cookie : cookies){
			    		if(cookie.getName().equals("user")){
			    			loginCookie = cookie;
			    			break;
			    		}
			    	}
		    	}
		    	if(loginCookie != null){
		    		loginCookie.setMaxAge(0);
		        	response.addCookie(loginCookie);
		    	}
		    	String url = "/logout";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
			else if(btn.equals("ToMyReview")){
				String url = "/myreviews";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
		}
	}
	
	
	
	
	
	
	
	
	private void displayHotelTable(PrintWriter out){
		List<Hotel> hotels = dbhandler.getHotels();
		if(hotels != null){
			out.println("<table>");
			out.println("\t<tr>");
			out.println("\t\t<th>HotelName</th>");
			out.println("\t\t<th>HotelAddress</th>");
			out.println("\t\t<th>HotelCountry</th>");
			out.println("\t\t<th>Rating</th>");
			out.println("\t\t<th>SearchAttractionWithin</th>");
			out.println("\t</tr>");
			for(int i = 0; i < hotels.size(); i++){
				Hotel hotel = hotels.get(i);
				String hotelid = hotel.getHotelId();
				String hotelname = hotel.getHotelName();
				String hoteladdr = hotel.getAddress_string();
				String hotelcountry = hotel.getCountry();
				List<Review> reviews = dbhandler.getReviews(hotelid);
				double rating = 0;
				int size = reviews.size();
				for(int j = 0; j < size; j++){
					Review review = reviews.get(j);
					rating += review.getRating();
				}
				out.println("\t<tr>");
				out.println("\t\t<td>" + "<a href=\"/reviews?hotelId=" + hotelid + "\">" + hotelname + "</a>" + "</td>");
				out.println("\t\t<td>" + hoteladdr + "</td>");
				out.println("\t\t<td>" + hotelcountry + "</td>");
				if(size != 0){
					rating /= size;
					out.println("\t\t<td>" + String.format("%.1f", rating) + "</td>");
				}
				else{
					out.println("\t\t<td>" + "Not Available" + "</td>");
				}
				out.println("\t\t<td>");
				out.println("\t\t\t<form action=\"/hotelInfo\" method=\"post\">");
				out.println("HotelId: <input style=\"background-color: lightblue\" type=\"text\" name=\"hotelid\" value=\"" + hotelid + "\"readonly>");
				out.println("<input type=\"text\" name=\"radius\" placeholder=\"0.5 - 10 in miles\">");
				out.println("<p><input type=\"submit\" name=\"btn\" value=\"Search\"></p>");
				out.println("\t\t\t</form>");
				out.println("\t\t</td>");
				out.println("\t</tr>");
			}
			out.println("</table>");
		}
	}
	
	
	
	
	
	
	
	private void toMyRevieButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/hotelInfo\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"ToMyReview\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	
	private void logoutButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/hotelInfo\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Logout\"></p>");
		out.println("</form>");
	}
	
}
