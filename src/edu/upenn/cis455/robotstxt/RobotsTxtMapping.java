package edu.upenn.cis455.robotstxt;

import edu.upenn.cis455.httpclient.RequestError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RobotsTxtMapping {

    private static Map<String, RobotsTxt> mapping = new HashMap<>();

    public static RobotsTxt get(String domain) {
        RobotsTxt robotsTxt = mapping.get(domain);
        if (robotsTxt == null) {
            try {
                RobotsTxt robotsTxt1 = RobotsTxt.fetch(domain);
                if (robotsTxt1 == null) {
                    return null;
                } else {
                    mapping.put(domain, robotsTxt1);
                    return robotsTxt1;
                }
            } catch (IOException | RobotsTxtSyntaxError | RequestError e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return robotsTxt;
        }
    }
}
