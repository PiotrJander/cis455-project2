package edu.upenn.cis455.servlet;

import com.sleepycat.je.DatabaseException;
import edu.upenn.cis455.storage.DBWrapper;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            writer.println("<p>User name: " + request.getSession().getAttribute("username") + "<a href='/logout'>Logout</a></p>");

            channels(writer);

            writer.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void channels(PrintWriter writer) {
        Map<String, String> channels = new HashMap<>();
        channels.put("foo", "bar");
        channels.put("abc", "def");
        channels.put("xyz", "baz");

        List<String> subscriptions = Arrays.asList("foo", "abc");

        writer.println("<dd>");

        channels.forEach((k, v) -> {
            writer.format("<dt>%s</dt>\n", k);
            writer.format("<dd>%s</dd>\n", v);

            if (subscriptions.contains(k)) {
                writer.format("<dd><a href='/show?channel=%s'>Show</a></dd>\n", k);
                writer.format("<dd><a href='/subscribe?channel=%s&unsubscribe=true'>Unsubscribe</a></dd>\n", k);
            } else {
                writer.format("<dd><a href='/subscribe?channel=%s'>Subscribe</a></dd>\n", k);
            }
        });

        writer.println("</dd>");
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









