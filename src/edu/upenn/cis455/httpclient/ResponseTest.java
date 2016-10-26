package edu.upenn.cis455.httpclient;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

public class ResponseTest extends TestCase {

    public void testParse() throws Exception {

        String responseText =
                "HTTP/1.1 200 OK_200\n" +
                "Content-Type: text/html\n" +
                "\n";
        Response response = new Response(
                new BufferedReader(
                        new StringReader(responseText)), HttpMethod.GET);
        response.parse();
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals("text/html", response.getHeader("Content-Type"));
    }
}