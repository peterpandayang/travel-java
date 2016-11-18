package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;






@SuppressWarnings("serial")
public class LandingServlet extends BaseServlet {
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Prepare landgin page", response);
		System.out.println("Landing page started...");
		PrintWriter out = response.getWriter();
		displaySubmit(out); 
		toHotelInfoButton(out);
		finishResponse(response);
	}
	
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Landing page", response);
		
		String btn = request.getParameter("btn");
		
		if(btn.equals("Register")){
			String url = "/register";
			url = response.encodeRedirectUrl(url);
			response.sendRedirect(url);
		}
		else if(btn.equals("Login")){
			String url = "/login";
			url = response.encodeRedirectUrl(url);
			response.sendRedirect(url);
		}
		else if(btn.equals("HotelInfo")){
			String url = "/hotelInfo";
			url = response.encodeRedirectUrl(url);
			response.sendRedirect(url);
		}
	}

	
	
	
	
	
	
	
	
	
	/** Writes and HTML for submit */
	private void displaySubmit(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/landing\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Register\"></p>");
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"Login\"></p>");
		out.println("</form>");
	}
	
	
	
	
	
	
	
	
	private void toHotelInfoButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/landing\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
		out.println("</form>");
	}
}
