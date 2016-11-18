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
public class EditReviewServlet extends BaseServlet{
	
	
	// DatabaseHandler interacts with the MySQL database
		private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
		
		
		
		public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException {

			prepareResponse("Loading Profile", response);
			System.out.println("Loading profile...");
			PrintWriter out = response.getWriter();
							
			String reviewId = request.getParameter("reviewId");
			
			String hotelId = dbhandler.getHotelIdByReviewId(reviewId);
			List<Hotel> hotels = dbhandler.getHotels();
			
			String hotelName = getHotelNameById(hotelId, hotels);
//			System.out.println("hotelname: "+ hotelName);
			
			String username = null;
			Cookie[] cookies = request.getCookies();
			if(cookies !=null){
				for(Cookie cookie : cookies){
					if(cookie.getName().equals("user")) {
						username = cookie.getValue();
					}
				}

				if(username != null){
					String userId = dbhandler.getUserIdByName(username);
					Review review = dbhandler.getOneReview(reviewId, hotelId);
					String title = review.getReviewTitle();
					String text = review.getReviewText();
					int rating = review.getRating();
					addReviewForm(out, reviewId, hotelName, userId, title, text, rating);
				}
				else{
					toLoginButton(out);
				}
			}
			else{
				toLoginButton(out);
			}
			finishResponse(response);
		}
		
		
		
		
		
		
		
		
		
		@SuppressWarnings("deprecation")
		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws IOException {
			prepareResponse("Edit Review", response);

			String btn = request.getParameter("btn");
//			System.out.println("btn is " + btn);
			String reviewId = request.getParameter("reviewid");
			if(btn != null){				
				if(btn.equals("Update")){
					String hotelId = dbhandler.getHotelIdByReviewId(reviewId);
					String userId = request.getParameter("userid");
					String title = request.getParameter("title");
					String text = request.getParameter("text");
					String rating = request.getParameter("rating");
					
					boolean isValid = check(rating);
					if(isValid){
						System.out.println("start update...");
						int ratingInt = Integer.parseInt(rating);
						dbhandler.deleteOneReview(reviewId);
						dbhandler.addOneReview(reviewId, hotelId, userId, title, text, ratingInt);
						response.getWriter().println("Edit review successful!");
						String url = "/myreviews";
						url = response.encodeRedirectUrl(url);
						response.sendRedirect(url);
					}
					else{
						response.getWriter().println("Invalid rating!");
						String url = "/hotelInfo";
						url = response.encodeRedirectUrl(url);
						response.sendRedirect(url);
					}
				}
				else if(equals("ToMyReview")){
					String url = "/myreviews";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				}
				else if(btn.equals("Delete")){
					System.out.println("start deleting...");
					dbhandler.deleteOneReview(reviewId);
					String url = "/myreviews";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				}else if(btn.equals("Login")){
					String url = "/login";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				}
			}
			else{
				response.getWriter().println("No button!");
				String url = "/hotelInfo";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
			finishResponse(response);
		}
		
		
		
		
		
		
		
		
		
		private boolean check(String rating){
			if(rating.equals("1") || rating.equals("2") || rating.equals("3") || rating.equals("4") || rating.equals("5")){
				return true;
			}
			return false;
		}
		
		
		
		
		
		
		
		
		
		private String getHotelNameById(String hotelId, List<Hotel> hotels){
			String hotelName = null;
			for(Hotel hotel : hotels){
				if(hotel.getHotelId().equals(hotelId)){
					hotelName = hotel.getHotelName();
				}
			}
			return hotelName;
		}
		
		
		
		
		
		
		
		
		
		/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
		private void addReviewForm(PrintWriter out, String reviewId, String hotelName, String userId, String title, String text, int rating) {
			assert out != null;

			out.println("<form action=\"/editreview\" method=\"post\">"); // the form will be processed by POST
			out.println("<table border=\"0\">");
			out.println("\t<tr>");
			out.println("\t\t<td>ReviewId:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"reviewid\" size=\"30\" value=\"" + reviewId + "\"readonly></td>");
			out.println("\t</tr>");
			out.println("\t<tr>");
			out.println("\t\t<td>HotelName:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"hotelname\" size=\"30\" value=\"" + hotelName + "\"readonly></td>");
			out.println("\t</tr>");
			out.println("\t<tr>");
			out.println("\t\t<td>UserId:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"userid\" size=\"30\" value=\"" + userId + "\"readonly></td>");
			out.println("\t</tr>");
			out.println("\t<tr>");
			out.println("\t\t<td>Title:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"title\" size=\"30\" value=\"" + title + "\"></td>");
			out.println("</tr>");
			out.println("\t<tr>");
			out.println("\t\t<td>Text:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"text\" size=\"30\" value=\"" + text + "\"></td>");
			out.println("</tr>");
			out.println("\t<tr>");
			out.println("\t\t<td>Rating:</td>");
			out.println("\t\t<td><input type=\"text\" name=\"rating\" size=\"30\" value=\"" + rating + "\"></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Update\"></p>");
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Delete\"></p>");
			out.println("</form>");
			
		}
		
		
		
		
		
		
		
		
		
		
		private void toLoginButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/editreview\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
			out.println("</form>");
		}
		
		
		
		
		
		
		
//		private void updateButton(PrintWriter out){
//			assert out != null;
//
//			out.println("<form action=\"/editreview\" method=\"post\">"); // the form will be processed by POST
//			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Update\"></p>");
//			out.println("</form>");
//		}
//		
//		
//		
//		
//		
//		
//		
//		private void deleteButton(PrintWriter out){
//			assert out != null;
//
//			out.println("<form action=\"/editreview\" method=\"post\">"); // the form will be processed by POST
//			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Delete\"></p>");
//			out.println("</form>");
//		}
		
		
}




