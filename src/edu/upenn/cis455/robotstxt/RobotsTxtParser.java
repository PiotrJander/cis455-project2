package edu.upenn.cis455.robotstxt;

import edu.upenn.cis455.crawler.XPathCrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

class RobotsTxtParser {

    private String domain;
    private int thisAgentCrawlDelay = 0;
    private int allAgentsCrawlDelay = 0;
    private ArrayList<String> disallow = new ArrayList<>();
    private BufferedReader reader;

    RobotsTxtParser(String domain, BufferedReader reader) {
        this.domain = domain;
        this.reader = reader;
    }

    RobotsTxt parse() throws IOException, RobotsTxtSyntaxError {
        noAgentState();
        int crawlDelay = thisAgentCrawlDelay > allAgentsCrawlDelay ? thisAgentCrawlDelay : allAgentsCrawlDelay;
        return new RobotsTxt(domain, crawlDelay, disallow);
    }

    private void noAgentState() throws IOException, RobotsTxtSyntaxError {
        String line;
        while (Objects.equals(line = reader.readLine(), "")) ;
        if (line == null) {
            return;
        } else {
            Directive directive = Directive.parse(line);
            if (directive.isNameEqual("user-agent")) {
                switch (directive.value) {
                    case "*":
                        relevantAgentState(false);
                    case XPathCrawler.name:
                        relevantAgentState(true);
                    default:
                        foreignAgentState();
                }
            } else {
                throw new RobotsTxtSyntaxError("Record must begin with User-Agent directive.");
            }
        }
    }

    private void foreignAgentState() throws IOException, RobotsTxtSyntaxError {
        String line;
        do {
            line = reader.readLine();
        } while (!Objects.equals(line, "") && line != null);
        if (line != null) {
            noAgentState();
        }
    }

    private void relevantAgentState(boolean thisAgent) throws IOException, RobotsTxtSyntaxError {
        String line = reader.readLine();
        while (line != null && !line.equals("")) {
            Directive directive = Directive.parse(line);
            if (directive.isNameEqual("disallow")) {
                disallow.add(directive.value);
            } else if (directive.isNameEqual("crawl-delay")) {
                int delay;
                try {
                    delay = Integer.parseInt(directive.value);
                } catch (NumberFormatException e) {
                    throw new RobotsTxtSyntaxError("Crawl-Delay must be a number");
                }

                if (thisAgent) {
                    thisAgentCrawlDelay = delay;
                } else {
                    allAgentsCrawlDelay = delay;
                }
            }

            line = reader.readLine();
        }
        if (line != null) {
            noAgentState();
        }
    }
}

class Directive {

    String name;
    String value;

    private Directive(String name, String value) {
        this.name = name.toLowerCase();
        this.value = value;
    }

    static Directive parse(String lineString) throws RobotsTxtSyntaxError {
        String[] nameValue = lineString.split(": ");
        if (nameValue.length == 2) {
            return new Directive(nameValue[0], nameValue[1].trim());
        } else {
            throw new RobotsTxtSyntaxError("Invalid directive.");
        }
    }

    boolean isNameEqual(String s) {
        return name.equals(s.toLowerCase());
    }
}