package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.database.DatabaseHandler;





@SuppressWarnings("serial")
public class AddReviewServlet extends BaseServlet{
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
	
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Loading Profile", response);
		System.out.println("Loading profile...");
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
				String userId = dbhandler.getUserIdByName(username);
				String reviewId = generateReviewId(userId);
				addReviewForm(out, reviewId, hotelId, userId);
				toMyReviewButton(out);
				toLogoutButton(out);
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
		prepareResponse("Add Review", response);

		String btn = request.getParameter("btn");
		
		if(btn != null){
			if(btn.equals("AddReview")){
				String reviewId = request.getParameter("reviewid");
				String hotelId = request.getParameter("hotelid");
				String userId = request.getParameter("userid");
				String title = request.getParameter("title");
				String text = request.getParameter("text");
				String rating = request.getParameter("rating");
				
				boolean isValid = check(rating);
				if(isValid){
					int ratingInt = Integer.parseInt(rating);
					dbhandler.addOneReview(reviewId, hotelId, userId, title, text, ratingInt);
					response.getWriter().println("Add review successful!");
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
			else if(btn.equals("ToMyReview")){
				String url = "/myreviews";
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
		}
		else{
			response.getWriter().println("No button!");
			String url = "/hotelInfo";
			url = response.encodeRedirectUrl(url);
			response.sendRedirect(url);
		}
		finishResponse(response);
	}
	
	
	
	
	
	
	
	
	private String generateReviewId(String userId){
		Random random = new Random();
		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = dbhandler.encodeHex(saltBytes, 32); // hash salt
		String reviewId = dbhandler.getHash(userId, usersalt);
		return reviewId;
	}
	
	
	
	
	
	
	
	private boolean check(String rating){
		if(rating.equals("1") || rating.equals("2") || rating.equals("3") || rating.equals("4") || rating.equals("5")){
			return true;
		}
		return false;
	}
	
	
	
	

	
	
	
	/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
	private void addReviewForm(PrintWriter out, String reviewId, String hotelId, String userId) {
		assert out != null;

		out.println("<form action=\"/addreview\" method=\"post\">"); // the form will be processed by POST
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>ReviewId:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"reviewid\" size=\"30\" value=\"" + reviewId + "\"readonly></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>HotelId:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"hotelid\" size=\"30\" value=\"" + hotelId + "\"readonly></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>UserId:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"userid\" size=\"30\" value=\"" + userId + "\"readonly></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Title:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"title\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Text:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"text\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Rating:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"rating\" size=\"30\" placeholder=\"1 to 5\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"AddReview\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	private void toMyReviewButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/addreview\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"ToMyReview\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	private void toLogoutButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/addreview\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Logout\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	private void toLoginButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/profile\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
		out.println("</form>");
	}		
	
}
