package edu.upenn.cis455.robotstxt;

import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.time.Instant;
import java.util.List;

public class RobotsTxt {

    private String domain;
    private int crawlDelay;
    private List<String> disallow;
    private Instant lastAccess;
    // TODO set this last access

    public RobotsTxt(String domain, Integer crawlDelay, List<String> disallow) {
        this.domain = domain;
        this.crawlDelay = crawlDelay;
        this.disallow = disallow;
    }

    public String getDomain() {
        return domain;
    }

    public int getCrawlDelay() {
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

    public static RobotsTxt fetch(String domain) throws IOException, RequestError, RobotsTxtSyntaxError {
        URL robotsTxtUrl = new URL("http", domain, "/robots.txt");
        Request request = new Request("GET", robotsTxtUrl);
        Response response = request.fetch();

        switch (response.getStatus()) {
            case OK_200:
                BufferedReader stringReader = new BufferedReader(new StringReader(response.getBody()));
                return (new RobotsTxtParser(domain, stringReader)).parse();
            default:
                return null;
        }
    }

}
