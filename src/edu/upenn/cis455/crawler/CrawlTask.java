package edu.upenn.cis455.crawler;

import edu.upenn.cis455.httpclient.HttpStatus;
import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;
import edu.upenn.cis455.storage.DBWrapper;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.IOException;
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
                // TODO add urls to frontier anyway
                return new LinkedList<>();
            default:
                reportSkippingUrl("Request or server error");
                return new LinkedList<>();
        }
    }

    private List<URL> makeGetRequest() throws RequestError, IOException {
        Request getRequest = new Request("GET", url);
        setIfModifiedSince(getRequest);
        Response getResponse = getRequest.fetch();

        if (getResponse.getStatus() == HttpStatus.OK_200 && checkResponseHeaders(getResponse)) {
            return processDocument(getResponse);
        } else {
            return new LinkedList<>();
        }
    }

    private List<URL> processDocument(Response getResponse) throws IOException {
        int contentLength = getResponse.getIntHeader("Content-Length").orElse(null);
        ContentType contentType = getContentType(getResponse.getHeader("Content-Type"));
        switch (contentType) {
            case XML:
                return processXml(getResponse, contentLength);
            case HTML:
            case XHTML:
                processHtml(getResponse);
            default:
                return new LinkedList<>();
        }
    }

    private List<URL> processHtml(Response getResponse) {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        StringWriter stringWriter = new StringWriter();
        Document document = tidy.parseDOM(getResponse.getReader(), stringWriter);
        String xhtml = stringWriter.toString();
        System.out.println(xhtml);  // TODO remove
        DBWrapper.addDocument(url, xhtml);
        return new LinkedList<>();
    }

    private List<URL> processXml(Response getResponse, int contentLength) throws IOException {
        char[] buffer = new char[contentLength];
        int length = getResponse.getReader().read(buffer, 0, contentLength);
        String documentText = String.valueOf(buffer, 0, length);
        DBWrapper.addDocument(url, documentText);
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
        } else if (contentType.equals("text/html")) {
            return ContentType.HTML;
        } else if (contentType.equals("application/xhtml+xml")) {
            return ContentType.XHTML;
        } else if (contentType.equals("text/xml") || contentType.equals("application/xml") || contentType.endsWith("+xml")) {
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

