package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cs601.database.DatabaseHandler;
import cs601.enumeration.Status;







@SuppressWarnings("serial")
public class RegistrationServlet extends BaseServlet {
	
	// DatabaseHandler interacts with the MySQL database
	private static final DatabaseHandler dbhandler = DatabaseHandler.getInstance();

	
	
	

	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Register New User", response);
		System.out.println("Register started...");
		PrintWriter out = response.getWriter();
		
		// error will not be null if we were forwarded her from the post method where something went wrong
		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		displayForm(out); 
		finishResponse(response);
	}

	
	
	
	
	
	
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Register New User", response);

		String btn = request.getParameter("btn");
		
		if(btn != null && btn.equals("Landing")){
			String url = "/landing";
			url = response.encodeRedirectUrl(url);
			response.sendRedirect(url);
			return;
		}
		
		
		// Get data from the textfields of the html form
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		// sanitize user input to avoid XSS attacks:
		newuser = StringEscapeUtils.escapeHtml4(newuser);
		newpass = StringEscapeUtils.escapeHtml4(newpass);
		
		System.out.println("Do post for register");
		
		// add user's info to the database 
		Status status = dbhandler.registerUser(newuser, newpass);

		if(status == Status.OK) { // registration was successful
			response.getWriter().println("Registered! Database updated.");
			toLandingPageButton(response.getWriter());
		}
		else { // if something went wrong
			response.getWriter().println("registration error: " + status.message());
			toLandingPageButton(response.getWriter());
		}
		finishResponse(response);
	}

	
	
	
	
	
	
	
	/** Writes and HTML form that shows two textfields and a button to the PrintWriter */
	private void displayForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/register\" method=\"post\">"); // the form will be processed by POST
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
		out.println("<p><input type=\"submit\" value=\"Register\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	
	
	
	/** Writes and HTML for submit */
	private void toLandingPageButton(PrintWriter out) {
		assert out != null;
		out.println("<form action=\"/register\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Landing\"></p>");
		out.println("</form>");
	}
	
}
