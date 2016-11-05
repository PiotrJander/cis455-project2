package edu.upenn.cis455.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CreateChannelServlet")
public class CreateChannelServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String xpath = request.getParameter("xpath");

        if (xpath == null || pathInfo == null) {
            response.sendError(400, "usage: /create/<channel-name>?xpath=<pattern>");
        }

        response.getWriter().println(pathInfo);
        response.getWriter().println(xpath);

        // db logic; maybe validation

        // redirect here
    }
}
