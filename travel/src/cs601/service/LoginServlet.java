package cs601.service;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.enumeration.Status;










@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	
	
	

	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Login User", response);
		System.out.println("Login started...");
		PrintWriter out = response.getWriter();
		
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		displayForm(out); 
		toLandingPageButton(out);
		toHotelInfoButton(out);
		finishResponse(response);
	}

	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Login User", response);

		String btn = request.getParameter("btn");
		
		
		if(btn != null){
			if(btn.equals("Landing")){
				String url = "/landing";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			} 
			else if(btn.equals("Login")){
				// Get data from the textfields of the html form
				String user = request.getParameter("user");
				String pass = request.getParameter("pass");
				// sanitize user input to avoid XSS attacks:
				user = StringEscapeUtils.escapeHtml4(user);
				pass = StringEscapeUtils.escapeHtml4(pass);
								
				Status status = dbhandler.loginUser(user, pass);				
				if(status == Status.OK) { // login was successful
					response.getWriter().println("You're logged in!");
					Cookie loginCookie = new Cookie("user",user);
					//setting cookie to expiry in 30 mins
					loginCookie.setMaxAge(30*60);
					response.addCookie(loginCookie);
					
					String url = "/profile";
					url = response.encodeRedirectUrl(url);
					response.sendRedirect(url);
				}
				
				else { // if something went wrong
					response.getWriter().println("login error: " + status.message());
					toLandingPageButton(response.getWriter());
					toHotelInfoButton(response.getWriter());
				}
			}
			else if(btn.equals("HotelInfo")){
				String url = "/hotelInfo";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
		}
		finishResponse(response);
	}

	
	
	
	
	
	
	
	/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
	private void displayForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/login\" method=\"post\">"); // the form will be processed by POST
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	
	
	/** Writes and HTML for submit */
	private void toLandingPageButton(PrintWriter out) {
		assert out != null;
		out.println("<form action=\"/login\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Landing\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	private void toHotelInfoButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/login\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
		out.println("</form>");
	}

}
