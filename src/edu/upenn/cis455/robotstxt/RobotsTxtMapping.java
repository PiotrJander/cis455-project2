package edu.upenn.cis455.robotstxt;

import edu.upenn.cis455.httpclient.RequestError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RobotsTxtMapping {

    private Map<String, RobotsTxt> mapping = new HashMap<>();

    public RobotsTxt get(String domain) {
        if (mapping.containsKey(domain)) {
            return mapping.get(domain);
        } else {
            try {
                RobotsTxt robotsTxt = RobotsTxt.fetch(domain);
                mapping.put(domain, robotsTxt);
                return get(domain);
            } catch (IOException | RobotsTxtSyntaxError | RequestError e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
