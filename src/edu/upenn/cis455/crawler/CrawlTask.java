package edu.upenn.cis455.crawler;

import edu.upenn.cis455.httpclient.HttpStatus;
import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;
import edu.upenn.cis455.storage.DBWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static edu.upenn.cis455.httpclient.HttpStatus.NOT_MODIFIED_304;

class CrawlTask {

    private URL url;

    CrawlTask(URL url) {
        this.url = url;
    }

    List<URL> run() {
        try {
            return makeHeadRequest();
        } catch (SocketTimeoutException e) {
            System.out.println("Connection timeout.");
        } catch (RequestError | IOException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    private List<URL> makeHeadRequest() throws RequestError, IOException {
        Request headRequest = new Request("HEAD", url);
        setIfModifiedSince(headRequest);
        Response headResponse = headRequest.fetch();

        switch (headResponse.getStatus()) {
            case OK_200:
                if (checkResponseHeaders(headResponse)) {
                    System.out.println("Downloading " + url);
                    return makeGetRequest();
                } else {
                    return new LinkedList<>();
                }
            case MOVED_PERMANENTLY_301:
                return handleMovedPermanently(headResponse);
            case FOUND_302:
                reportSkippingUrl("Temporary redirects not supported.");
                return new LinkedList<>();
            case NOT_MODIFIED_304:
                reportSkippingUrl("Not modified since the last retrieval.");
                return new LinkedList<>();
            default:
                reportSkippingUrl("Request or server error");
                return new LinkedList<>();
        }
    }

    private List<URL> makeGetRequest() throws RequestError, IOException {
        Request request = new Request("GET", url);
        setIfModifiedSince(request);
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
                return processXml(response, contentLength);
            case HTML:
            case XHTML:
                return processHtml(response);
            default:
                return new LinkedList<>();
        }
    }

    private List<URL> processHtml(Response response) {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        StringWriter stringWriter = new StringWriter();
        Document document = tidy.parseDOM(new StringReader(response.getBody()), stringWriter);

        // meta robots
        NodeList metaTags = document.getElementsByTagName("meta");


        String xhtml = stringWriter.toString();
        System.out.println(xhtml);  // TODO remove
        DBWrapper.addDocument(url, xhtml);
        return extractUrls();
    }

    private List<URL> extractUrls() {
        return new LinkedList<>();
    }

    private List<URL> processXml(Response response, int contentLength) throws IOException {
        DBWrapper.addDocument(url, response.getBody());
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
            reportSkippingUrl("Chunked encoding not supported.");
            return false;
        }

        Optional<Integer> contentLength = headResponse.getIntHeader("Content-Length");
        if (contentLength.isPresent()) {
            if (contentLength.orElse(null) > XPathCrawler.getMaxSize()) {
                reportSkippingUrl("Content-Length exceeds the limit.");
                return false;
            }
        } else {
            reportSkippingUrl("Content-Length not specified.");
            return false;
        }

        ContentType contentType = getContentType(headResponse.getHeader("Content-Type"));
        if (contentType == ContentType.OTHER) {
            reportSkippingUrl("Content type other than HTML or XML");
            return false;
        }

        return true;
    }

    private void setIfModifiedSince(Request headRequest) {
        Date lastRetrievedDate = DBWrapper.getDocumentDate(url);
        if (lastRetrievedDate != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(lastRetrievedDate.toInstant(), ZoneOffset.UTC);
            String formattedDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            headRequest.setHeader("If-Modified-Since", formattedDate);
        }
    }

    private void reportSkippingUrl(String reason) {
        System.out.println(String.format("Skippig %s; %s", url, reason));
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
}


enum ContentType {
    HTML,
    XHTML,
    XML,
    OTHER
}

