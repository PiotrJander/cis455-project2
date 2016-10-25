package edu.upenn.cis455.crawler;

import edu.upenn.cis455.httpclient.Request;
import edu.upenn.cis455.httpclient.RequestError;
import edu.upenn.cis455.httpclient.Response;
import edu.upenn.cis455.storage.DBWrapper;

import java.io.IOException;
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

    List<URL> run() {
        try {
            if (isHeadRequestSuccessful()) {
                return getRequest();
            } else {
                return new LinkedList<>();
            }
        } catch (RequestError | IOException e) {  // SocketTimeoutException is subclass of IOException
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    private List<URL> getRequest() throws RequestError, IOException {
        Request request = new Request("GET", url);
        Response response = request.fetch();

        Optional<Integer> contentLength = response.getIntHeader("Content-Length");
        String documentText;
        if (contentLength.isPresent()) {
            char[] buffer = new char[contentLength.orElse(null)];
            int length = response.getReader().read(buffer, 0, contentLength.orElse(null));
            documentText = String.valueOf(buffer, 0, length);
        } else {
            return new LinkedList<>();
        }

        ContentType contentType = getContentType(response.getHeader("Content-Type"));
        switch (contentType) {
            case XML:
                DBWrapper.addDocument(url, documentText);
                return new LinkedList<>();
            case HTML:
            case XHTML:
                ;
            case OTHER:
                return new LinkedList<>();
        }
    }

    private boolean isHeadRequestSuccessful() throws RequestError, SocketTimeoutException {
        Request headRequest = new Request("HEAD", url);
        Response headResponse = headRequest.fetch();

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
