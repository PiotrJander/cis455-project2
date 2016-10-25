package edu.upenn.cis455.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {

    private BufferedReader in;
    private HttpStatus status;
    private Map<String, String> headers = new HashMap<>();

    public Response(BufferedReader in) {
        this.in = in;
    }

    void parse() throws IOException, BadResponse {
        parseFirstLine();
        parseHeaders();

        // get body
    }

    private void parseHeaders() throws IOException {
        String line;
        while (!Objects.equals(line = in.readLine(), "")) {
            String[] header = line.split(": ");
            headers.put(header[0], header[1]);
        }
    }

    private void parseFirstLine() throws IOException, BadResponse {
        String first = in.readLine();
        Pattern pattern = Pattern.compile("HTTP/1.1\\s+([1-5][0-9][0-9])");
        Matcher matcher = pattern.matcher(first);
        if (matcher.matches()) {
            try {
                status = HttpStatus.getByCode(Integer.parseInt(matcher.group(1)));
            } catch (IllegalArgumentException e) {  // NumberFormatException is a subclass
                throw new BadResponse();
            }
        } else {
            throw new BadResponse();
        }
    }
}

class BadResponse extends Exception {}
