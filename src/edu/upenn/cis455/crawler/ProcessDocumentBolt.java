package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

import java.util.Map;

public class ProcessDocumentBolt implements IRichBolt {
    @Override
    public String getExecutorId() {
        return null;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {

    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {

    }

    @Override
    public void setRouter(IStreamRouter router) {

    }

    @Override
    public Fields getSchema() {
        return null;
    }
}
