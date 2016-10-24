package edu.upenn.cis455.servlet;

import edu.upenn.cis455.storage.DBWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class AuthServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            response.sendError(400, "A request to this endpoint must contain `username` and `password` parameters.");
        } else {
            Optional<String> dbPassword = DBWrapper.getUserPassword(username);
            if (dbPassword.isPresent()) {
                // compare passwords, if equal, log in, if not, redirect to login page
                if (dbPassword.orElse(null).equals(password)) {
                    request.getSession().setAttribute("username", username);
                } else {
                    response.sendRedirect("xpath");
                }
            } else {
                // create new user
                DBWrapper.addUser(username, password);
                response.sendRedirect("xpath");
            }
        }
    }
}
