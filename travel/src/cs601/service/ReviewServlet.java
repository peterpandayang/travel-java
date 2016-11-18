package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.entity.Hotel;
import cs601.entity.Review;






@SuppressWarnings("serial")
public class ReviewServlet extends BaseServlet{

	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
	
	
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Entering review info page", response);
		System.out.println("Reviews info...");
		PrintWriter out = response.getWriter();
		String hotelId = request.getParameter("hotelId");
		
		String username = null;
		Cookie[] cookies = request.getCookies();
		if(cookies !=null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("user")) {
					username = cookie.getValue();
				}
			}

			if(username != null){
				toAddReviewButton(out, hotelId);
			}
			else{
				out.println("you need to login!");
				toLoginButton(out);
			}
		}
		else{
			out.println("you need to login!");
			toLoginButton(out);
		}		
		displayReviewTable(out, hotelId);
		finishResponse(response);
	}
	
	
	
	
	
	
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Add review", response);

		String btn = request.getParameter("btn");
		
		if(btn != null){
			if(btn.equals("Login")){
				String url = "login";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
			else{
				String hotelId = request.getParameter("hotelId");
				String url = "/addreview?hotelId=" + hotelId;
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			} 
		}
	}
	
	
	
	
	
	
	
	
	
	
	private void displayReviewTable(PrintWriter out, String hotelid){
		List<Review> reviews = dbhandler.getReviews(hotelid);
		List<Hotel> hotels = dbhandler.getHotels();
		String hotelname = null;
		for(Hotel hotel : hotels){
			if(hotel.getHotelId().equals(hotelid)){
				hotelname = hotel.getHotelName();
				break;
			}
		}
		if(reviews != null){
			out.println("<table>");
			out.println("\t<tr>");
			out.println("\t\t<th>HotelName</th>");
			out.println("\t\t<th>ReviewTitle</th>");
			out.println("\t\t<th>ReviewText</th>");
			out.println("\t\t<th>Rating</th>");
			out.println("\t</tr>");
			for(int i = 0; i < reviews.size(); i++){
				Review review = reviews.get(i);
				String reviewid = review.getReviewId();
				String title = review.getReviewTitle();
				String text = review.getReviewText();
				int rating = review.getRating();
				out.println("\t<tr>");
				out.println("\t\t<td>" + hotelname + "</td>");
				out.println("\t\t<td>" + title + "</td>");
				out.println("\t\t<td>" + text + "</td>");
				out.println("\t\t<td>" + rating + "</td>");
				out.println("\t</tr>");
			}
			out.println("</table>");
		}
	}
	
	
	
	
	
	
	
	private void toAddReviewButton(PrintWriter out, String hotelId){
		assert out != null;

		out.println("<form action=\"/reviews\" method=\"post\">"); // the form will be processed by POST
		out.println("HotelId: <input type=\"text\" name=\"hotelId\" value=\"" + hotelId + "\"readonly>");
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"ToAddReview\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	private void toLoginButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/reviews\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
		out.println("</form>");
	}
	
}
