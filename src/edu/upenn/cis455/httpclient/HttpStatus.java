package edu.upenn.cis455.httpclient;

public enum HttpStatus {
    CONTINUE(100, "Continue"),
    OK(200, "OK"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented");

    private final int statusCode;
    private final String name;

    HttpStatus(int statusCode, String name) {
        this.statusCode = statusCode;
        this.name = name;
    }

    int getStatusCode() {
        return statusCode;
    }

    String getName() {
        return name;
    }

    public static HttpStatus getByCode(int statusCode) {
        for (HttpStatus status : HttpStatus.class.getEnumConstants()) {
            if (status.statusCode == statusCode) {
                return status;
            }
        }
        return null;
    }

    public static HttpStatus getByCode(String statusCode) throws IllegalArgumentException {
        try {
            int intCode = Integer.parseInt(statusCode);
            for (HttpStatus status : HttpStatus.class.getEnumConstants()) {
                if (status.statusCode == intCode) {
                    return status;
                }
            }
        } catch (NumberFormatException ignore) {}
        throw new IllegalArgumentException();
    }
}
