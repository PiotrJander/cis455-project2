package edu.upenn.cis455.robotstxt;

class RobotsTxtSyntaxError extends Exception {
    public RobotsTxtSyntaxError() { super(); }
    public RobotsTxtSyntaxError(String message) { super(message); }
    public RobotsTxtSyntaxError(String message, Throwable cause) { super(message, cause); }
    public RobotsTxtSyntaxError(Throwable cause) { super(cause); }
}
