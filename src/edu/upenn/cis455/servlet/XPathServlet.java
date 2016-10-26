package edu.upenn.cis455.servlet;

import com.sleepycat.je.DatabaseException;
import edu.upenn.cis455.storage.DBWrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.log4j.Logger;
// import org.apache.log4j.BasicConfigurator;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(XPathServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
//        BasicConfigurator.configure();
        try {
            super.init(config);
            String envDirPath = config.getServletContext().getInitParameter("BDBstore");
            DBWrapper.init(new File(envDirPath));
        } catch (DatabaseException e) {
            log.error(e);
            System.exit(1);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        DBWrapper.close();
    }

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		/* TODO: Implement user interface for XPath engine here */

		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession().getAttribute("username") == null) {
            // not logged
            loginForm(request, response);
        } else {
            homePage(request, response);
        }

    }

    private void homePage(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();
            writer.println("<html><head><title>Home page</title></head><body>");
            writer.println("<p>User name: " + request.getSession().getAttribute("username") + "</p>");
            writer.println("<a href=\"logout\">Logout</a>");
            writer.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loginForm(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();
            writer.println("<html><head><title>Home page</title></head><body>");
            writer.println("<form action=\"register.jsp\" method=\"post\">");
            writer.println("<p>Username <input type=\"text\" name=\"username\"></p>");
            writer.println("<p>Password <input type=\"password\" name=\"password\"></p>");
            writer.println("<p><input type=\"submit\"></p>");
            writer.println("</form>");
            writer.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}









