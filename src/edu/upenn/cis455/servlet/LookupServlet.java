package edu.upenn.cis455.servlet;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

@WebServlet(name = "LookupServlet")
public class LookupServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String encodedUrl = request.getParameter("url");
        PrintWriter body = response.getWriter();
        if (encodedUrl == null) {
            body.println("Usage: /lookup?url=<encoded-url>");
        } else {
            String url = URLDecoder.decode(encodedUrl, "UTF-8");
            Document document = DBWrapper.getDocument(url);
            if (document == null) {
                body.println(String.format("Url %s not found.", url));
            } else {
                body.print(document.getText());
            }
        }
    }
}
