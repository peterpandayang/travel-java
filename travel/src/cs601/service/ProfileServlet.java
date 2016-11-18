package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.entity.Hotel;
import cs601.entity.Review;
import cs601.enumeration.Status;





@SuppressWarnings("serial")
public class ProfileServlet extends BaseServlet{
		// DatabaseHandler interacts with the MySQL database
		private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();
		
		
		
		
		public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException {

			prepareResponse("Loading Profile", response);
			System.out.println("Loading profile...");
			PrintWriter out = response.getWriter();
			
			toHotelInfoButton(out);
			
			String username = null;
			Cookie[] cookies = request.getCookies();
			if(cookies != null){
				for(Cookie cookie : cookies){
					if(cookie.getName().equals("user")) {
						username = cookie.getValue();
					}
				}
				if(username != null){
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
			prepareResponse("Login User", response);

			String btn = request.getParameter("btn");
			
			
			if(btn != null){
				if(btn.equals("ToMyReview")){
					String url = "/myreviews";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				} 
				else if(btn.equals("HotelInfo")){
					String url = "/hotelInfo";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				} 
				else if(btn.equals("Login")){
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
			}
			finishResponse(response);
		}
		
	
		
		
		
		
		
		private void toMyReviewButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/profile\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"ToMyReview\"></p>");
			out.println("</form>");
		}
		
		
		
		
		
		
		private void toLoginButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/profile\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
			out.println("</form>");
		}
		
		
		
		
		
		
		
		private void toLogoutButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/profile\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Logout\"></p>");
			out.println("</form>");
		}
		
		
		
		
		
		
		
		private void toHotelInfoButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/profile\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
			out.println("</form>");
		}
		
}
