package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.storage.DBWrapper;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.DOMElementImpl;
import org.w3c.tidy.Tidy;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProcessDocumentBolt implements IRichBolt {

    private static Logger log = Logger.getLogger(ProcessDocumentBolt.class);
    private Fields fields = new Fields();
    private String executorId = UUID.randomUUID().toString();
    private OutputCollector collector;

    @Override
    public void execute(Tuple input) {
        URL url = (URL) input.getObjectByField("url");
        String contentType = input.getStringByField("contentType");
        String document = input.getStringByField("document");
        boolean shouldWriteToDb = input.getIntegerByField("shouldWriteToDb") != 0;

        switch (contentType) {
            case "xml":
                if (shouldWriteToDb) DBWrapper.addDocument(url, document, "xml");
            case "html":
            case "xhtml":
                processHtml(url, document, shouldWriteToDb);
        }
    }

    private void processHtml(URL url, String document, boolean shouldWriteToDb) {
        StringWriter stringWriter = new StringWriter();
        Document dom = parseHtml(document, stringWriter);
        String metaRobots = getMetaRobots(dom);

        if (shouldWriteToDb && (metaRobots == null || !metaRobots.contains("noindex"))) {
            String xhtml = stringWriter.toString();
            DBWrapper.addDocument(url, xhtml, "xhtml");
        }

        if (metaRobots == null || !metaRobots.contains("nofollow")) {
            extractUrls(dom, url);
        }
    }

    private Document parseHtml(String document, StringWriter stringWriter) {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setShowErrors(0);
        tidy.setQuiet(true);
        return tidy.parseDOM(new StringReader(document), stringWriter);
    }

    private String getMetaRobots(Document dom) {
        NodeList metaTags = dom.getElementsByTagName("meta");
        for (int i = 0; i < metaTags.getLength(); i++) {
            Node meta = metaTags.item(i);
            String name = getAttribute(meta, "name");
            if (Objects.equals(name, "robots")) {
                return getAttribute(meta, "content");
            }
        }
        return null;
    }

    private void extractUrls(Document dom, URL url) {
        URL contextUrl = getContextUrl(dom, url);
        NodeList anchorList = dom.getElementsByTagName("a");
        for (int i = 0; i < anchorList.getLength(); i++) {
            Node anchor = anchorList.item(i);
            String href = getAttribute(anchor, "href");
            if (href != null) {
                try {
                    collector.emit(new Values<>(new URL(contextUrl, href)));
                } catch (MalformedURLException ignored) {
                }
            }
        }
    }

    /**
     * By default relative URLs are resolved against the document URL, but this can be overridden by the `base` tag,
     * which itself is resolved against the document URL.
     */
    private URL getContextUrl(Document document, URL url) {
        try {
            NodeList baseTagList = document.getElementsByTagName("base");
            if (baseTagList.getLength() > 0) {
                Node baseTag = baseTagList.item(0);
                String href = getAttribute(baseTag, "href");
                if (href != null) {
                    return new URL(url, href);
                }
            }
        } catch (MalformedURLException ignored) {
        }
        return url;
    }

    private String getAttribute(Node n, String s) {
        return ((DOMElementImpl) n).getAttribute("href");
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(fields);
    }

    @Override
    public void setRouter(IStreamRouter router) {
        // Do nothing
    }

    @Override
    public Fields getSchema() {
        return fields;
    }
}
