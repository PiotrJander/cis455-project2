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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class CrawlTask {

    private URL url;

    CrawlTask(URL url) {
        this.url = url;
    }

    List<URL> run() {
        try {
            if (isHeadRequestSuccessful()) {
                return getRequest();
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Connection timeout.");
        } catch (RequestError | IOException e) {  // SocketTimeoutException is subclass of IOException
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    private List<URL> getRequest() throws RequestError, IOException {
        Request request = new Request("GET", url);
        Response response = request.fetch();

        Optional<Integer> contentLength = response.getIntHeader("Content-Length");
//        String documentText;
//        if (contentLength.isPresent()) {
//            char[] buffer = new char[contentLength.orElse(null)];
//            int length = response.getReader().read(buffer, 0, contentLength.orElse(null));
//            response.getReader().close();
//            documentText = String.valueOf(buffer, 0, length);
//        } else {
//            return new LinkedList<>();
//        }

        ContentType contentType = getContentType(response.getHeader("Content-Type"));
        switch (contentType) {
            case XML:
//                DBWrapper.addDocument(url, documentText);
                // TODO best way to get string from response
                return new LinkedList<>();
            case HTML:
            case XHTML:
                Tidy tidy = new Tidy();
                tidy.setXHTML(true);
                StringWriter stringWriter = new StringWriter();
                Document document = tidy.parseDOM(response.getReader(), stringWriter);
                String xhtml = stringWriter.toString();
                System.out.println(xhtml);  // TODO remove
                DBWrapper.addDocument(url, xhtml);
            default:
                return new LinkedList<>();
        }
    }

    private boolean isHeadRequestSuccessful() throws RequestError, SocketTimeoutException {
        Request headRequest = new Request("HEAD", url);
        setIfModifiedSince(headRequest);
        Response headResponse = headRequest.fetch();

        if (headResponse.getStatus() == HttpStatus.NOT_MODIFIED) {
            reportHeadFailure("Not modified since the last retrieval.");
            return false;
        }

        if (headResponse.getHeader("Transfer-Encoding") != null) {
            reportHeadFailure("Chunked encoding not supported.");
            return false;
        }

        Optional<Integer> contentLength = headResponse.getIntHeader("Content-Length");
        if (contentLength.isPresent()) {
            if (contentLength.orElse(null) > XPathCrawler.getMaxSize()) {
                reportHeadFailure("Content-Length exceeds the limit.");
                return false;
            }
        } else {
            reportHeadFailure("Content-Length not specified.");
            return false;
        }

        ContentType contentType = getContentType(headResponse.getHeader("Content-Type"));
        if (contentType == ContentType.OTHER) {
            reportHeadFailure("Content type other than HTML or XML");
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

    private void reportHeadFailure(String reason) {
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


class CrawlingException extends Exception {

    private String reason;

    CrawlingException(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
