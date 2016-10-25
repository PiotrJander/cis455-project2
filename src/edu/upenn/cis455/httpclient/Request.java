package edu.upenn.cis455.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private HttpMethod method;
    private URL url;
    private Map<String, String> headers = new HashMap<>();

    public Request(String method, String url) {
        try {
            this.method = HttpMethod.valueOf(method);
            this.url = new URL(url);
            setRequiredHeaders();
        } catch (IllegalArgumentException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Request(HttpMethod method, URL url) {
        this.method = method;
        this.url = url;
        setRequiredHeaders();
    }

    private void setRequiredHeaders() {
        setHeader("Host", this.url.getHost());
        setHeader("User-Agent", "cis455crawler");
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public Response fetch() throws RequestError {
        try (
            Socket echoSocket = new Socket(url.getHost(), url.getPort());
            PrintWriter out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
        ) {
            writeRequest(out);

            return getResponse(in);
        } catch (IOException | BadResponse e) {
            throw new RequestError();
        }
    }

    public void writeRequest(PrintWriter out) {
        out.println(String.format("%s %s HTTP/1.1", method.name(), url.toString()));
        for (Map.Entry header : headers.entrySet()) {
            out.println(header.getKey() + ": " + header.getValue());
        }
        out.println();
    }

    public Response getResponse(BufferedReader in) throws IOException, BadResponse {
        Response response = new Response(in);
        response.parse();
        return response;
    }
}


class RequestError extends Exception {}