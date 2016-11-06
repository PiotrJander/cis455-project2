package test.edu.upenn.cis455;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

import java.util.List;

public class XPathEngineImplTest extends TestCase {

    public void testTokenize() throws Exception {
        XPathEngineImpl engine = new XPathEngineImpl();
        List<XPathEngineImpl.Token> tokens0 = engine.tokenize("/foo/bar");

        assertEquals(4, tokens0.size());

        XPathEngineImpl.Token token0 = tokens0.get(0);
        assertEquals(XPathEngineImpl.TokenType.AXIS, token0.type);

        XPathEngineImpl.Token token1 = tokens0.get(1);
        assertEquals(XPathEngineImpl.TokenType.NODENAME, token1.type);
        assertEquals("foo", token1.match);

        List<XPathEngineImpl.Token> tokens1 = engine.tokenize("/,=[]()");
        assertEquals(XPathEngineImpl.TokenType.AXIS, tokens1.get(0).type);
        assertEquals(XPathEngineImpl.TokenType.COMMA, tokens1.get(1).type);
        assertEquals(XPathEngineImpl.TokenType.EQUALS, tokens1.get(2).type);
        assertEquals(XPathEngineImpl.TokenType.LEFT_BRACKET, tokens1.get(3).type);
        assertEquals(XPathEngineImpl.TokenType.RIGHT_BRACKET, tokens1.get(4).type);
        assertEquals(XPathEngineImpl.TokenType.LEFT_PARENTHESIS, tokens1.get(5).type);
        assertEquals(XPathEngineImpl.TokenType.RIGHT_PARENTHESIS, tokens1.get(6).type);

        List<XPathEngineImpl.Token> tokens2 = engine.tokenize("text() containss contains foo");
        assertEquals(XPathEngineImpl.TokenType.TEXT, tokens2.get(0).type);
        assertEquals(XPathEngineImpl.TokenType.WHITESPACE, tokens2.get(1).type);
        assertEquals(XPathEngineImpl.TokenType.NODENAME, tokens2.get(2).type);
        assertEquals(XPathEngineImpl.TokenType.WHITESPACE, tokens2.get(3).type);
        assertEquals(XPathEngineImpl.TokenType.CONTAINS, tokens2.get(4).type);
        assertEquals(XPathEngineImpl.TokenType.WHITESPACE, tokens2.get(5).type);
        assertEquals(XPathEngineImpl.TokenType.NODENAME, tokens2.get(6).type);

        List<XPathEngineImpl.Token> tokens3 = engine.tokenize("@att\"foo bar\"");
        assertEquals(XPathEngineImpl.TokenType.ATTNAME, tokens3.get(0).type);
        assertEquals(XPathEngineImpl.TokenType.STRING, tokens3.get(1).type);
        assertEquals("\"foo bar\"", tokens3.get(1).match);

    }

}