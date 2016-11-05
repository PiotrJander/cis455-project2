package edu.upenn.cis455.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "ShowChannelServlet")
public class ShowChannelServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String channelName = request.getParameter("channel");

        if (channelName == null) {
            response.sendError(400, "usage: /show?channel=<name>");
        }

        // get channel here
        Channel channel = new Channel("news", "piotr", Arrays.asList(
                new Document("foo/bar", ZonedDateTime.now(), "foo"),
                new Document("baz/bar", ZonedDateTime.now(), "foo")
        ));

        PrintWriter writer = response.getWriter();
        writer.println("<html><head><title>Home page</title></head><body>");

        writer.println("<div class='channelheader'>");
        writer.format("<h1>Channel name: %s</h1>\n", channel.name);
        writer.format("<p>Created by: %s</p>\n", channel.createdBy);
        writer.println("</div>");

        channel.documents.forEach(document -> {
            writer.println("<div class='match'>");
            String date = document.dateTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.format("<p>Crawled on: %s</p>\n", date);
            writer.format("<p>Location: %s</p>\n", document.url);
            writer.format("<div class='document'>%s</div>\n", document.body);
            writer.println("</div>");

        });

        writer.println("</body></html>");
    }

    class Channel {
        String name;
        String createdBy;
        List<Document> documents;

        public Channel(String name, String createdBy, List<Document> documents) {
            this.name = name;
            this.createdBy = createdBy;
            this.documents = documents;
        }
    }

    class Document {
        String url;
        ZonedDateTime dateTime;
        String body;

        public Document(String url, ZonedDateTime dateTime, String body) {
            this.url = url;
            this.dateTime = dateTime;
            this.body = body;
        }
    }
}
