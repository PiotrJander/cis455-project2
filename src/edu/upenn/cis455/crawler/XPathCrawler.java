package edu.upenn.cis455.crawler;


import com.sleepycat.je.DatabaseException;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis455.storage.DBWrapper;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class XPathCrawler {

    public static final String name = "cis455crawler";
    private static URL startUrl;
    private final String URL_SPOUT = "URL_SPOUT";
    private final String CRAWLER_BOLT = "CRAWLER_BOLT";
    private final String PROCESS_DOCUMENT_BOLT = "PROCESS_DOCUMENT_BOLT";
    private final String MERGE_URLS_BOLT = "MERGE_URLS_BOLT";

//    private static int maxSize;
//    private static int maxDocuments = 1000;
//    private static int visitedDocumentsCount = 0;
//
//    public static void incrementVisitedDocumentCount() {
//        visitedDocumentsCount++;
//    }
//
//    static int getMaxSize() {
//        return maxSize;
//    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        parseArguments(args);

        UrlFrontier.INSTANCE.add(startUrl);
        UrlFrontier.INSTANCE.add(startUrl);
        XPathCrawler crawler = new XPathCrawler();
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

//        maxSize = parseInt(args[2]) << 20;  // arg max size given in megabytes, convert to bytes
//
//        if (args.length == 4) {
//            maxDocuments = parseInt(args[3]);
//        }
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
        Config config = new Config();
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(URL_SPOUT, new UrlSpout(), 1);
        builder.setBolt(CRAWLER_BOLT, new CrawlerBolt(), 1).fieldsGrouping(URL_SPOUT, new Fields("domain"));
        builder.setBolt(PROCESS_DOCUMENT_BOLT, new ProcessDocumentBolt(), 1).shuffleGrouping(CRAWLER_BOLT);
        builder.setBolt(MERGE_URLS_BOLT, new MergeUrlsBolt(), 1).fieldsGrouping(PROCESS_DOCUMENT_BOLT, new Fields("domain"));

        LocalCluster cluster = new LocalCluster();
        Topology topo = builder.createTopology();

        cluster.submitTopology("crawler", config, builder.createTopology());

        // TODO maybe handle interrupt signal here?

        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        cluster.killTopology("crawler");
//        cluster.shutdown();
//        System.exit(0);

        DBWrapper.close();
    }
}
