package cs601.server;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.json.simple.JSONObject;

import cs601.entity.ThreadSafeHotelData;
import cs601.service.AddReviewServlet;
import cs601.service.AttractionServlet;
import cs601.service.EditReviewServlet;
import cs601.service.HotelInfoServlet;
import cs601.service.InvalidRadiusServlet;
import cs601.service.LandingServlet;
import cs601.service.LoginServlet;
import cs601.service.LogoutServlet;
import cs601.service.MyReviewServlet;
import cs601.service.ProfileServlet;
import cs601.service.RegistrationServlet;
import cs601.service.ReviewServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;






public class MainServer extends Thread{

	public static final int PORT = 4050;
	private static ThreadSafeHotelData hdata = null;
	
	
	
	
	public MainServer(ThreadSafeHotelData hdata){
		this.hdata = hdata;
	}
	

	
	
	
	
	
	/**
	 * start the server
	 */
	public void run(){
		
		Server server = new Server(PORT);
		
		ServletHandler handler = new ServletHandler();
		
		
		ResourceHandler resourceHandler = new ResourceHandler(); 
        resourceHandler.setDirectoriesListed(false); 
        ContextHandler context = new ContextHandler(); 
        resourceHandler.setWelcomeFiles(new String[]{ "/views/landing.html" }); 
        resourceHandler.setResourceBase("./public");
//        context.setContextPath("/landing"); 
        
        context.setHandler(resourceHandler);
      
//		server.setHandler(context);
        
        
        // regular path
//        ServletContextHandler reviewsContext = new ServletContextHandler();
        

		handler.addServletWithMapping(LandingServlet.class, "/landing");
		handler.addServletWithMapping(RegistrationServlet.class, "/register");
		handler.addServletWithMapping(LoginServlet.class, "/login");
		handler.addServletWithMapping(LogoutServlet.class, "/logout");
		handler.addServletWithMapping(HotelInfoServlet.class, "/hotelInfo");
		handler.addServletWithMapping(ReviewServlet.class, "/reviews");
		handler.addServletWithMapping(ProfileServlet.class, "/profile");
		handler.addServletWithMapping(MyReviewServlet.class, "/myreviews");
		handler.addServletWithMapping(AddReviewServlet.class, "/addreview");
		handler.addServletWithMapping(EditReviewServlet.class, "/editreview");
		handler.addServletWithMapping(AttractionServlet.class, "/attractions");
		handler.addServletWithMapping(InvalidRadiusServlet.class, "/invalidradius");
		
		
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {resourceHandler, handler});
		
		server.setHandler(handlers);
//		server.setHandler(context);
		
		try {
			server.start();
			System.out.println("Server started...");
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}