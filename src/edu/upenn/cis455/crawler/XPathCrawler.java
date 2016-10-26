package edu.upenn.cis455.crawler;


import com.sleepycat.je.DatabaseException;
import edu.upenn.cis455.storage.DBWrapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class XPathCrawler {

    public static final String name = "cis455crawler";

    private static URL startUrl;
    private static int maxSize;
    private static int maxDocuments = 1000;
    private static int visitedDocumentsCount = 0;

    private LinkedList<URL> frontier = new LinkedList<>();
    private Set<URL> visited = new HashSet<>();

    private XPathCrawler(URL start) {
        frontier.add(start);
    }

    XPathCrawler() {
    }

    private static void incrementVisitedDocumentCount() {
        visitedDocumentsCount++;
    }

    static int getMaxSize() {
        return maxSize;
    }

    public static void main(String[] args) {
        parseArguments(args);

        XPathCrawler crawler = new XPathCrawler(startUrl);
        crawler.crawl();
    }

    private static void parseArguments(String[] args) {
        if (args.length < 3 || args.length > 4) {
            usage();
        }

        try {
            startUrl = new URL(args[0]);
        } catch (MalformedURLException e) {
            System.err.println("Invalid start URL");
            usage();
        }

        try {
            File dbEnvDir = new File(args[1]);
            DBWrapper.init(dbEnvDir);
        } catch (DatabaseException e) {
            System.err.println("Database error");
            e.printStackTrace();
            usage();
        }

        maxSize = parseInt(args[2]) << 20;  // arg max size given in megabytes, convert to bytes

        if (args.length == 4) {
            maxDocuments = parseInt(args[3]);
        }
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            usage();
            return 0;
        }
    }

    private static void usage() {
        System.err.println("java -jar crawler.jar <start-startUrl> <berkeley-db-evn-dir> <max-size> [max-documents]");
        System.exit(1);
    }

    private void crawl() {
        while (visitedDocumentsCount < maxDocuments && !frontier.isEmpty()) {
            URL next = frontier.remove();
            CrawlTask task = new CrawlTask(next);
            List<URL> newUrls = task.run();

            // in the special case that crawl delay hasn't elapsed yet, we move the URL
            // to the end of the queue; and we don't add it to visited
            if (newUrls.size() > 0 && !Objects.equals(newUrls.get(0), next)) {
                incrementVisitedDocumentCount();
                visited.add(next);
            }

            for (URL url : newUrls) {
                if (!visited.contains(url)) {
                    frontier.add(url);
                }
            }
        }
        DBWrapper.close();
    }
}
