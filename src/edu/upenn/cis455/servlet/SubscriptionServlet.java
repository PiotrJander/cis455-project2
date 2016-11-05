package edu.upenn.cis455.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@WebServlet(name = "SubscriptionServlet")
public class SubscriptionServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String channel = request.getParameter("channel");

        if (channel == null) {
            response.sendError(400, "usage: /subscribe?channel=<name>[&unsubscribe=true]");
        }

        String unsubscribe = request.getParameter("unsubscribe");
        if (Objects.equals(unsubscribe, "true")) {
            response.getWriter().println("Unsubscribed from " + channel);
        } else {
            response.getWriter().println("Subscribed to " + channel);
        }

        // TODO redirect
    }
}
