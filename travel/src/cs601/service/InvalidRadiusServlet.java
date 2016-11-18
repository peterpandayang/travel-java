package cs601.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;






@SuppressWarnings("serial")
public class InvalidRadiusServlet extends BaseServlet {
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Entering hotel info page", response);
		PrintWriter out = response.getWriter();
		response.getWriter().println("radius should be 0.5 to 10 miles!");
		toHotelInfoButton(out); 
		finishResponse(response);
	}

	
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Go to search tourist sites", response);
		String btn = request.getParameter("btn");
		
		if(btn != null){
			if(btn.equals("HotelInfo")){
				String url = "/hotelInfo";
				url = response.encodeRedirectUrl(url);
				response.sendRedirect(url);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	private void toHotelInfoButton(PrintWriter out){
		assert out != null;

		out.println("<form action=\"/invalidradius\" method=\"post\">"); // the form will be processed by POST
		out.println("<p><input type=\"submit\" name=\"btn\" value=\"HotelInfo\"></p>");
		out.println("</form>");
	}
	
}





