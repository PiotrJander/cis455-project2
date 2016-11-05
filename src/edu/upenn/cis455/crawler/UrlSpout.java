package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.UrlFrontier;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class UrlSpout implements IRichSpout {

    private static Logger log = Logger.getLogger(UrlSpout.class);
    private String executorId = UUID.randomUUID().toString();
    private SpoutOutputCollector collector;

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "domain"));
    }

    @Override
    public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
        log.setLevel(Level.INFO);
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public void nextTuple() {
        URL url = UrlFrontier.INSTANCE.poll();
        if (url != null) {
            log.debug(getExecutorId() + " emitting " + url);
            this.collector.emit(new Values<>(url, url.getHost()));
        }

        // TODO do I need this?
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }
}
