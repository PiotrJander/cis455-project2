package edu.upenn.cis455.httpclient;

public enum HttpStatus {
    CONTINUE_100(100, "Continue"),
    OK_200(200, "OK_200"),
    MOVED_PERMANENTLY_301(301, "Moved Permanently"),
    FOUND_302(302, "Found"),
    NOT_MODIFIED_304(304, "Not Modified"),
    TEMPORARY_REDIRECT_307(307, "Temporary Redirect"),
    PERMANENT_REDIRECT_308(308, "Permanent Redirect"),
    BAD_REQUEST_400(400, "Bad Request"),
    NOT_FOUND_404(404, "Not Found"),
    METHOD_NOT_ALLOWED_405(405, "Method Not Allowed"),
    PRECONDITION_FAILED_412(412, "Precondition Failed"),
    INTERNAL_SERVER_ERROR_500(500, "Internal Server Error"),
    NOT_IMPLEMENTED_501(501, "Not Implemented");

    private final int statusCode;
    private final String name;

    HttpStatus(int statusCode, String name) {
        this.statusCode = statusCode;
        this.name = name;
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

    int getStatusCode() {
        return statusCode;
    }

    String getName() {
        return name;
    }
}
