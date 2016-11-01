package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.UrlFrontier;
import edu.upenn.cis455.httpclient.HttpStatus;
import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;
import edu.upenn.cis455.robotstxt.RobotsTxt;
import edu.upenn.cis455.robotstxt.RobotsTxtMapping;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Document;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CrawlerBolt implements IRichBolt {

    private static Logger log = Logger.getLogger(CrawlerBolt.class);
    private Fields fields = new Fields("contentType", "document");
    private String executorId = UUID.randomUUID().toString();
    private OutputCollector collector;

    private RobotsTxtMapping robotsTxtMapping = new RobotsTxtMapping();
    private URL url;

    @Override
    public void execute(Tuple input) {
        url = (URL) input.getObjectByField("url");
        RobotsTxt robotsTxt = robotsTxtMapping.get(url.getHost());
        if (robotsTxt == null) {
            makeHeadRequest();
        } else if (!robotsTxt.isPathAllowed(url.getPath())) {
            printRetrievalStatus("Restricted. Not downloading");
        } else if (!robotsTxt.isTimeElapsed()) {
            UrlFrontier.INSTANCE.add(url);
        } else {
            robotsTxt.updateLastAccessedTime();
            makeHeadRequest();
        }
    }

    private void makeHeadRequest() {
        try {
            Request request = new Request("HEAD", url);
            edu.upenn.cis455.storage.Document document = DBWrapper.getDocument(url);
            if (document != null) setIfModifiedSince(request, document.getDateRetrieved());
            Response response = request.fetch();

            switch (response.getStatus()) {
                case OK_200:
                    handleOkResponse(response);
                case MOVED_PERMANENTLY_301:
                    handleMovedPermanently(response);
                case FOUND_302:
                    notDownloadingLogReason("Temporary redirects not supported");
                case NOT_MODIFIED_304:
                    handleNotModified(document);
                default:
                    notDownloadingLogReason("Request or server error, or unsupported response code");
            }
        } catch (RequestError | IOException e) {
            notDownloadingLogReason("Uncaught exception");
            log.warn(e);
        }
    }

    private void handleOkResponse(Response response) throws IOException, RequestError {
        if (shouldCrawlUrl(response)) {
            makeGetRequest();
        }
    }

    private void makeGetRequest() throws RequestError, IOException {
        Request request = new Request("GET", url);
        Response response = request.fetch();

        if (response.getStatus() == HttpStatus.OK_200 && shouldCrawlUrl(response)) {
            printRetrievalStatus("Downloading");
            String contentType = getContentType(response);
            collector.emit(new Values<>(contentType, response.getBody()));
        } else {
            notDownloadingLogReason("HEAD suceeded but GET failed");
        }
    }

    private boolean shouldCrawlUrl(Response response) {
        if (response.getHeader("Transfer-Encoding") != null) {
            notDownloadingLogReason("Chunked encoding not supported.");
            return false;
        }

        Optional<Integer> contentLength = response.getIntHeader("Content-Length");
        if (contentLength.isPresent()) {
            if (contentLength.orElse(null) > XPathCrawler.getMaxSize()) {
                notDownloadingLogReason("Content-Length exceeds the limit.");
                return false;
            }
        } else {
            notDownloadingLogReason("Content-Length not specified.");
            return false;
        }

        String contentType = getContentType(response);
        if (Objects.equals(contentType, "OTHER")) {
            notDownloadingLogReason("Content type other than HTML or XML");
            return false;
        }

        return true;
    }

    private String getContentType(Response response) {
        String contentTypeHeader = response.getHeader("Content-Type");
        if (contentTypeHeader == null) {
            return "OTHER";
        } else if (contentTypeHeader.contains("text/html")) {
            return "HTML";
        } else if (contentTypeHeader.contains("application/xhtml+xml")) {
            return "XHTML";
        } else if (contentTypeHeader.contains("text/xml") || contentTypeHeader.contains("application/xml") || contentTypeHeader.contains("+xml")) {
            return "XML";
        } else {
            return "OTHER";
        }
    }

    private void notDownloadingLogReason(String reason) {
        printRetrievalStatus("Not Downloading");
        log.warn(String.format("Not Downloading %s; reason: %s", url, reason));
    }

    private void setIfModifiedSince(Request headRequest, Date lastRetrievedDate) {
        if (lastRetrievedDate != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(lastRetrievedDate.toInstant(), ZoneOffset.UTC);
            String formattedDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            headRequest.setHeader("If-Modified-Since", formattedDate);
        }
    }

    private void handleNotModified(Document document) throws MalformedURLException {
        printRetrievalStatus("Not modified");
        collector.emit(new Values<>(document.getContentType(), document.getText()));
    }

    private void handleMovedPermanently(Response headResponse) throws MalformedURLException {
        String location = headResponse.getHeader("Location");
        if (location != null) {
            UrlFrontier.INSTANCE.add(new URL(url, location));
        }
    }

    private void printRetrievalStatus(String s) {
        System.out.format("%s: %s\n", url, s);
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public void setRouter(IStreamRouter router) {
        // Do nothing
    }

    @Override
    public Fields getSchema() {
        return fields;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(fields);
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }
}
