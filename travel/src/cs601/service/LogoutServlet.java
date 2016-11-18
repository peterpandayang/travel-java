package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.enumeration.Status;







public class LogoutServlet extends BaseServlet {
	// DatabaseHandler interacts with the MySQL database
		private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

		
		
		

		
		
		public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException {

			prepareResponse("Logout User", response);
			System.out.println("Logout...");
			PrintWriter out = response.getWriter();
			
			// error will not be null if we were forwarded her from the post method where something went wrong
			String error = request.getParameter("error");
			if(error != null) {
				String errorMessage = getStatusMessage(error);
				out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
			}

			toLandingPageButton(out);
			toHotelInfoButton(out);
			finishResponse(response);
		}

		
		
		
		
		
		@SuppressWarnings("deprecation")
		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws IOException {
			prepareResponse("Logout User", response);

			String btn = request.getParameter("btn");
			
			
			if(btn != null){
				if(btn.equals("Landing")){
					String url = "/landing";
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
		
		
		
		
		
		
		
		/** Writes and HTML for submit */
		private void toLandingPageButton(PrintWriter out) {
			assert out != null;
			out.println("<form action=\"/logout\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"Landing\"></p>");
			out.println("</form>");
		}
		
		
		
		
		
		
		
		private void toHotelInfoButton(PrintWriter out){
			assert out != null;

			out.println("<form action=\"/logout\" method=\"post\">"); // the form will be processed by POST
			out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
			out.println("</form>");
		}
}


