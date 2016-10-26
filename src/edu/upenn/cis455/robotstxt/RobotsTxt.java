package edu.upenn.cis455.robotstxt;

import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class RobotsTxt {

    private String domain;
    private int crawlDelay;
    private List<String> disallow;
    private Instant lastAccess;

    RobotsTxt(String domain, Integer crawlDelay, List<String> disallow) {
        this.domain = domain;
        this.crawlDelay = crawlDelay;
        this.disallow = disallow;
        this.lastAccess = Instant.now().minusSeconds(10);
    }

    static RobotsTxt fetch(String domain) throws IOException, RequestError, RobotsTxtSyntaxError {
        URL robotsTxtUrl = new URL("http", domain, "/robots.txt");
        Request request = new Request("GET", robotsTxtUrl);
        Response response = request.fetch();

        switch (response.getStatus()) {
            case OK_200:
                BufferedReader stringReader = new BufferedReader(new StringReader(response.getBody()));
                return (new RobotsTxtParser(domain, stringReader)).parse();
            case MOVED_PERMANENTLY_301:
            case FOUND_302:
            case TEMPORARY_REDIRECT_307:
            case PERMANENT_REDIRECT_308:
                String location = response.getHeader("Location");
                if (location == null) {
                    return null;
                } else {
                    return fetchRedirect(domain, new URL(robotsTxtUrl, location));
                }
            default:
                return null;
        }
    }

    private static RobotsTxt fetchRedirect(String domain, URL location) throws IOException, RequestError, RobotsTxtSyntaxError {
        Request request = new Request("GET", location);
        Response response = request.fetch();

        switch (response.getStatus()) {
            case OK_200:
                BufferedReader stringReader = new BufferedReader(new StringReader(response.getBody()));
                return (new RobotsTxtParser(domain, stringReader)).parse();
            default:
                return null;
        }
    }

    String getDomain() {
        return domain;
    }

    int getCrawlDelay() {
        return crawlDelay;
    }

    public boolean isPathAllowed(String path) {
        for (String prefix : disallow) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTimeElapsed() {
        return Duration.between(lastAccess, Instant.now()).getSeconds() > crawlDelay;
    }

    public void updateLastAccessedTime() {
        lastAccess = Instant.now();
    }

}
