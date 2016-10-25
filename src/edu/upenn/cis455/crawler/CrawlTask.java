package edu.upenn.cis455.crawler;

import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CrawlTask {

    private URL url;

    public CrawlTask(URL url) {
        this.url = url;
    }

    List<URL> run() throws RequestError, SocketTimeoutException {
        if (headQualifies()) {
            // TODO we might want to repeat the checks from HEAD

            return null;
        } else {
            return new LinkedList<>();
        }
    }

    private boolean headQualifies() throws RequestError, SocketTimeoutException {
        try {
            Request headRequest = new Request("HEAD", url);
            Response headResponse = headRequest.fetch();

            if (headResponse.getHeader("Transfer-Encoding") != null) {
                throw new CrawlingException("Chunked encoding not supported.");
            }

            Optional<Integer> contentLength = headResponse.getIntHeader("Content-Length");
            if (contentLength.isPresent()) {
                if (contentLength.orElse(null) > XPathCrawler.getMaxSize()) {
                    throw new CrawlingException("Content-Length exceeds the limit.");
                }
            } else {
                throw new CrawlingException("Content-Length not specified.");
            }

            ContentType contentType = getContentType(headResponse.getHeader("Content-Type"));
            if (contentType == ContentType.OTHER) {
                throw new CrawlingException("Content type other than HTML or XML");
            }

            return true;
        } catch (CrawlingException e) {
            // TODO refactor to avoid exception
            System.out.println(String.format("Skippig %s; %s", url, e.getReason()));
            return false;
        }
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
    OTHER;
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