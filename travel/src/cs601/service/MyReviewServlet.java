package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.database.DatabaseHandler;
import cs601.entity.Hotel;
import cs601.entity.Review;






@SuppressWarnings("serial")
public class MyReviewServlet extends BaseServlet {
	
	
	
	
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
	
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Loading my reviews", response);
		System.out.println("Loading my reviews...");
		PrintWriter out = response.getWriter();
		toHotelInfoButton(out);
		String username = null;
		Cookie[] cookies = request.getCookies();
		if(cookies !=null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("user")) {
					username = cookie.getValue();
				}
			}

			if(username == null){
				out.println("you need to login!");
				toLoginButton(out);
			}
			else{
				System.out.println("Username is: " + username);
				toLogoutButton(out);
				displayMyReviewTable(out, username);
			}
		}
		else{
			toLoginButton(out);
			out.println("you need to login!");
		}
		finishResponse(response);
	}

	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Add Review", response);

		String btn = request.getParameter("btn");
		
		
		if(btn != null){
			if(btn.equals("Login")){
				String url = "/login";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
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
			else if(btn.equals("HotelInfo")){
				String url = "/hotelInfo";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
		}
		finishResponse(response);
	}
	
	
	
	
	
	
	
	
	private void displayMyReviewTable(PrintWriter out, String username){
		String userId = dbhandler.getUserIdByName(username);
		List<Review> reviews = dbhandler.getReviewByUserId(userId);     				
		out.println("<table>");
		out.println("\t<tr>");
		out.println("\t\t<th>HotelName</th>");
		out.println("\t\t<th>ReviewTitle</th>");
		out.println("\t\t<th>ReviewText</th>");
		out.println("\t\t<th>Rating</th>");
		out.println("\t</tr>");
		
		if(reviews != null){			
			for(Review review : reviews){
				String hotelname = null;
				String hotelid = review.getHotelId();
				List<Hotel> hotels = dbhandler.getHotels();
				for(Hotel hotel : hotels){
					if(hotel.getHotelId().equals(hotelid)){
						hotelname = hotel.getHotelName();
						break;
					}
				}
				
				String reviewid = review.getReviewId();
				String title = review.getReviewTitle();
				String text = review.getReviewText();
				int rating = review.getRating();
				
				out.println("\t<tr>");
				out.println("\t\t<td>" + "<a href=\"/editreview?reviewId=" + reviewid + "\">" + hotelname + "</td>");
				out.println("\t\t<td>" + title + "</td>");
				out.println("\t\t<td>" + text + "</td>");
				out.println("\t\t<td>" + rating + "</td>");
				out.println("\t</tr>");
			}
		}
		out.println("</table>");
	}
	
	
	
	
	
	
	
	private void toHotelInfoButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/myreviews\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	private void toLoginButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/myreviews\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	private void toLogoutButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/myreviews\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Logout\"></p>");
		out.println("</form>");
	}
	
}







