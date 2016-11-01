package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class UrlSpout implements IRichSpout {

    static Logger log = Logger.getLogger(UrlSpout.class);
    String executorId = UUID.randomUUID().toString();
    SpoutOutputCollector collector;

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url"));
    }

    @Override
    public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;

        // TODO

//        try {
//            log.debug(getExecutorId() + " opening file reader");
//            reader = new BufferedReader(new FileReader("words.txt"));
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Override
    public void close() {
        // TODO
    }

    @Override
    public void nextTuple() {
        // TODO
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }
}
