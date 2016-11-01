package edu.upenn.cis455.crawler;

import edu.upenn.cis455.httpclient.HttpStatus;
import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;
import edu.upenn.cis455.storage.DBWrapper;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.DOMElementImpl;
import org.w3c.tidy.Tidy;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class CrawlTask {

    private static Logger log = Logger.getLogger(CrawlTask.class);
    private URL url;

    CrawlTask(URL url) {
        this.url = url;
    }

    private static String getAttribute(Node n, String s) {
        return ((DOMElementImpl) n).getAttribute("href");
    }

    List<URL> run() {
//        try {
//            // check robots.txt and maybe start making requests
//            RobotsTxt robotsTxt = new RobotsTxt();
//            if (robotsTxt != null) {
//                if (robotsTxt.isPathAllowed(url.getPath())) {
//                    if (robotsTxt.isTimeElapsed()) {
//                        robotsTxt.updateLastAccessedTime();
//                        return makeHeadRequest();
//                    } else {
//                        // time hasn't elapsed yet; add the URL to the end of the queue
//                        return new LinkedList<>(Collections.singletonList(url));
//                    }
////                    return makeHeadRequest();
//                } else {
//                    printRetrievalStatus("Restricted. Not downloading");
//                }
//            } else {
//                return makeHeadRequest();
//            }
//        } catch (SocketTimeoutException e) {
//            System.out.println("Connection timeout.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return new LinkedList<>();
    }

    private List<URL> makeHeadRequest() throws RequestError, IOException {
        Request headRequest = new Request("HEAD", url);

        edu.upenn.cis455.storage.Document document = DBWrapper.getDocument(url);
        if (document != null) {
            setIfModifiedSince(headRequest, document.getDateRetrieved());
        }

        Response headResponse = headRequest.fetch();

        switch (headResponse.getStatus()) {
            case OK_200:
                if (checkResponseHeaders(headResponse)) {
                    printRetrievalStatus("Downloading");
                    return makeGetRequest();
                } else {
                    return new LinkedList<>();
                }
            case MOVED_PERMANENTLY_301:
                return handleMovedPermanently(headResponse);
            case FOUND_302:
                logRetrievalStatus("Temporary redirects not supported.");
                return new LinkedList<>();
            case NOT_MODIFIED_304:
                return handleNotModified(document);
            default:
                logRetrievalStatus("Request or server error");
                return new LinkedList<>();
        }
    }

    private List<URL> handleNotModified(edu.upenn.cis455.storage.Document document) throws MalformedURLException {
        printRetrievalStatus("Not modified");
//        if (document.isHtml()) {
//            return processHtml(document.getText());
//        } else {
//        }
        return new LinkedList<>();
    }

    private List<URL> makeGetRequest() throws RequestError, IOException {
        Request request = new Request("GET", url);
//        setIfModifiedSince(request);
        Response response = request.fetch();

        if (response.getStatus() == HttpStatus.OK_200 && checkResponseHeaders(response)) {
            return processDocument(response);
        } else {
            return new LinkedList<>();
        }
    }

    private List<URL> processDocument(Response response) throws IOException {
        int contentLength = response.getIntHeader("Content-Length").orElse(null);
        ContentType contentType = getContentType(response.getHeader("Content-Type"));
        switch (contentType) {
            case XML:
                return processXml(response.getBody());
            case HTML:
            case XHTML:
                return processHtml(response.getBody());
            default:
                return new LinkedList<>();
        }
    }

    private List<URL> processHtml(String content) throws MalformedURLException {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setShowErrors(0);
        tidy.setQuiet(true);
        StringWriter stringWriter = new StringWriter();
        Document document = tidy.parseDOM(new StringReader(content), stringWriter);

        String metaRobots = getMetaRobots(document);

        if (metaRobots == null || !metaRobots.contains("noindex")) {
            String xhtml = stringWriter.toString();
            DBWrapper.addDocument(url, xhtml, "html");
        }

        if (metaRobots == null || !metaRobots.contains("nofollow")) {
            return extractUrls(document);
        } else {
            return new LinkedList<>();
        }
    }

    private String getMetaRobots(Document document) {
        NodeList metaTags = document.getElementsByTagName("meta");
        for (int i = 0; i < metaTags.getLength(); i++) {
            Node meta = metaTags.item(i);

            String name = getAttribute(meta, "name");
            String content = getAttribute(meta, "content");

            if (Objects.equals(name, "robots")) {
                return content;
            }
        }
        return null;
    }

    private List<URL> extractUrls(Document document) throws MalformedURLException {

        URL contextUrl = getContextUrl(document);
        LinkedList<URL> urls = new LinkedList<>();

        NodeList anchorList = document.getElementsByTagName("a");
        for (int i = 0; i < anchorList.getLength(); i++) {
            Node anchor = anchorList.item(i);
            String href = getAttribute(anchor, "href");
            if (href != null) {
                urls.add(new URL(contextUrl, href));
            }
        }

        return urls;
    }

    private URL getContextUrl(Document document) throws MalformedURLException {
//        NodeList baseTagList = document.getElementsByTagName("base");
//        if (baseTagList.getLength() > 0) {
//            Node baseTag = baseTagList.item(0);
//            String href = getAttribute(baseTag, "href");
//            if (href != null) {
//                return new URL(url, href);
//            }
//        }
        return url;
    }

    private List<URL> processXml(String responseBody) throws IOException {
        DBWrapper.addDocument(url, responseBody, "html");
        return new LinkedList<>();
    }

    private List<URL> handleMovedPermanently(Response headResponse) throws MalformedURLException {
        String location = headResponse.getHeader("Location");
        if (location == null) {
            return new LinkedList<>();
        } else {
            return Collections.singletonList(new URL(location));
        }
    }

    private boolean checkResponseHeaders(Response headResponse) {
        if (headResponse.getHeader("Transfer-Encoding") != null) {
            logRetrievalStatus("Chunked encoding not supported.");
            return false;
        }

        Optional<Integer> contentLength = headResponse.getIntHeader("Content-Length");
        if (contentLength.isPresent()) {
            if (contentLength.orElse(null) > XPathCrawler.getMaxSize()) {
                logRetrievalStatus("Content-Length exceeds the limit.");
                return false;
            }
        } else {
            logRetrievalStatus("Content-Length not specified.");
            return false;
        }

        ContentType contentType = getContentType(headResponse.getHeader("Content-Type"));
        if (contentType == ContentType.OTHER) {
            logRetrievalStatus("Content type other than HTML or XML");
            return false;
        }

        return true;
    }

    private void setIfModifiedSince(Request headRequest, Date lastRetrievedDate) {
        if (lastRetrievedDate != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(lastRetrievedDate.toInstant(), ZoneOffset.UTC);
            String formattedDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            headRequest.setHeader("If-Modified-Since", formattedDate);
        }
    }

    private void logRetrievalStatus(String reason) {
        printRetrievalStatus("Not Downloading");
        log.warn(String.format("Skipping %s; %s", url, reason));
    }

    private void printRetrievalStatus(String s) {
        System.out.format("%s: %s\n", url, s);
    }

    private ContentType getContentType(String contentType) {
        if (contentType == null) {
            return ContentType.OTHER;
        } else if (contentType.contains("text/html")) {
            return ContentType.HTML;
        } else if (contentType.contains("application/xhtml+xml")) {
            return ContentType.XHTML;
        } else if (contentType.contains("text/xml") || contentType.contains("application/xml") || contentType.contains("+xml")) {
            return ContentType.XML;
        } else {
            return ContentType.OTHER;
        }
    }

    private enum ContentType {
        HTML,
        XHTML,
        XML,
        OTHER
    }
}

