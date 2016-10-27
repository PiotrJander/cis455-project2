package edu.upenn.cis455.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private HttpMethod method;
    private URL url;
    private Map<String, String> headers = new HashMap<>();

    private Request(String method, String url) {
        try {
            this.method = HttpMethod.valueOf(method);
            this.url = new URL(url);
            setRequiredHeaders();
        } catch (IllegalArgumentException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Request(String method, URL url) {
        try {
            this.method = HttpMethod.valueOf(method);
            this.url = url;
            setRequiredHeaders();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public Request(HttpMethod method, URL url) {
        this.method = method;
        this.url = url;
        setRequiredHeaders();
    }

    /**
     * Test method.
     */
    public static void main(String[] args) {
        Request request = new Request("GET", "http://foo.org/path");
        PrintWriter writer = new PrintWriter(System.out);
        request.writeRequest(writer);
        writer.flush();
        writer.close();
    }

    private void setRequiredHeaders() {
        setHeader("Host", this.url.getHost());
        setHeader("User-Agent", "cis455crawler");
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public Response fetch() throws RequestError, SocketTimeoutException {
        switch (url.getProtocol()) {
            case "http":
                return fetchHttp();
            case "https":
                throw new RequestError("https not supported yet");
//                return fetchHttpSecure();
            default:
                throw new RequestError("protocol not supported yet");
        }
    }

//    private Response fetchHttpSecure() throws RequestError {
//        try {
//            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.connect();
//            PrintWriter out =
//                    new PrintWriter(connection.getOutputStream(), true);
//            BufferedReader in =
//                    new BufferedReader(
//                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
//            // socket.setSoTimeout(10000);
//            writeRequest(out);
//            return (new Response(in, method)).parse();
//        } catch (IOException | BadResponseException e) {
//            throw new RequestError();
//        }
//    }

    private Response fetchHttp() throws RequestError {
        try (
                Socket socket = new Socket(url.getHost(), url.getDefaultPort());
                PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            // socket.setSoTimeout(10000);
            writeRequest(out);
            return (new Response(in, method)).parse();
        } catch (IOException | BadResponseException e) {
            throw new RequestError();
        }
    }

    private void writeRequest(PrintWriter out) {
        out.println(String.format("%s %s HTTP/1.1", method.name(), url.toString()));
        for (Map.Entry header : headers.entrySet()) {
            out.println(header.getKey() + ": " + header.getValue());
        }
        out.println();
    }
}
