package edu.upenn.cis455.robotstxt;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

public class RobotsTxtParserTest extends TestCase {

    public void testParse() throws Exception {

        String robotsTxtContents =
                "# Comment\n" +
                        "User-agent: googlebot\n" +
                        "Disallow: /foo/\n" +
                        "Crawl-delay: 10\n" +
                        "\n" +
                        "# Comment\n" +
                        "User-agent: cis455crawler # Comment\n" +
                        "Disallow: /bar/\n" +
                        "Disallow: /baz/ # Comment\n" +
                        "Crawl-delay: 15\n" +
                        "\n" +
                        "User-agent: *\n" +
                        "Disallow: /xyz/\n" +
                        "Disallow: /qwe/\n" +
                        "Crawl-delay: 20 # Comment\n";
        BufferedReader reader = new BufferedReader(new StringReader(robotsTxtContents));

        RobotsTxtParser parser = new RobotsTxtParser("foo.com", reader);
        RobotsTxt robotsTxt = parser.parse();

        assertEquals("foo.com", robotsTxt.getDomain());
        assertEquals(20, robotsTxt.getCrawlDelay());
        assertFalse(robotsTxt.isPathAllowed("/bar/path"));
        assertFalse(robotsTxt.isPathAllowed("/xyz/path"));
        assertTrue(robotsTxt.isPathAllowed("/foo/path"));
    }

}