package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPathEngineImpl implements XPathEngine {

    private LinkedHashMap<Pattern, String> tokenPatterns = new LinkedHashMap<>();

    private String[] xpaths;

    public XPathEngineImpl() {
        // Do NOT add arguments to the constructor!!

        // add token patterns
        addTokenPattern("\\w+", "whitespace");
    }

    private void addTokenPattern(String pattern, String name) {
        tokenPatterns.put(Pattern.compile(String.format("^(%s)(.*$)", pattern)), name);
    }

    public void setXPaths(String[] s) {
        // TODO maybe compile to abstract repr
        this.xpaths = s;
    }

    public boolean isValid(int i) {
        // TODO impl
        return false;
    }

    public boolean[] evaluate(Document d) {
        boolean[] ret = new boolean[xpaths.length];
        for (int i = 0; i < xpaths.length; i++) {
            // TODO correct ret[i] = isValid(i);
        }
        return ret;
    }

    public List<Token> tokenize(String s) {
        String remaining = s;
        List<Token> ret = new ArrayList<>();

        while (!Objects.equals(remaining, "")) {
            for (TokenType tt : TokenType.values()) {
                Matcher m = tt.pattern.matcher(remaining);
                if (m.matches()) {
                    String match = m.group(1);
                    ret.add(new Token(tt, match));
                    remaining = m.group(2);
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean isSAX() {
        return false;
    }

    @Override
    public boolean[] evaluateSAX(InputStream document, DefaultHandler handler) {
        throw new UnsupportedOperationException();
    }

    public enum TokenType {

        AXIS("/"),
        COMMA(","),
        EQUALS("="),
        LEFT_BRACKET("\\["),
        RIGHT_BRACKET("\\]"),
        LEFT_PARENTHESIS("\\("),
        RIGHT_PARENTHESIS("\\)"),

        TEXT("text\\(\\)"),
        CONTAINS("contains(?!\\w)"),
        NODENAME("\\w+"),

        STRING("\".*?\""),
        ATTNAME("@\\w+"),
        WHITESPACE("\\s+");

        final Pattern pattern;

        TokenType(String pattern) {
            this.pattern = Pattern.compile(String.format("(%s)(.*)", pattern));
        }
    }

    class ParseResult<C extends SyntaxTreeNode> {
        SyntaxTreeNode node;
        String remaning;
    }

    abstract class SyntaxTreeNode {
    }

    class XPath extends SyntaxTreeNode {
        Step step;

        XPath(Step step) {
            this.step = step;
        }
    }

    class Step extends SyntaxTreeNode {
        Tests tests;
        Rest rest;
    }

    class Rest extends SyntaxTreeNode {
        XPath xPath;
    }

    class Tests extends SyntaxTreeNode {
        Test test;
    }

    class Test extends SyntaxTreeNode {
        Condition condition;
        Tests tests;
    }

    abstract class Condition extends SyntaxTreeNode {
    }

    class NodeHasXPath extends Condition {
    }

    class TextEquals extends Condition {
    }

    class TextContains extends Condition {
    }

    class AttributeEquals extends Condition {
    }

    public class Token {

        public TokenType type;
        public String match;

        Token(TokenType type, String match) {
            this.type = type;
            this.match = match;
        }
    }

}
