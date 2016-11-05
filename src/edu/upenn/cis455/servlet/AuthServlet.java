package edu.upenn.cis455.servlet;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class AuthServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            response.sendError(400, "A request to this endpoint must contain `username` and `password` parameters.");
        } else {

            User user = DBWrapper.getUser(username);
            if (user != null && Objects.equals(user.getPassword(), password)) {
                request.getSession().setAttribute("username", username);
            } else if (user == null) {
                // create new user
                DBWrapper.addUser(username, password);
                request.getSession().setAttribute("username", username);
            }
        }
        response.sendRedirect("xpath");
    }
}
