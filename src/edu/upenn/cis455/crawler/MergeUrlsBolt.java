package edu.upenn.cis455.crawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MergeUrlsBolt implements IRichBolt {

    private static Logger log = Logger.getLogger(MergeUrlsBolt.class);
    private Fields fields = new Fields();
    private String executorId = UUID.randomUUID().toString();

    private Set<String> visited = new HashSet<>();

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(fields);
    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public void execute(Tuple input) {
        URL url = (URL) input.getObjectByField("url");
        if (!visited.contains(url.toString())) {
            UrlFrontier.INSTANCE.add(url);
            visited.add(url.toString());
        }
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        // Do nothing
    }

    @Override
    public void setRouter(IStreamRouter router) {
    }

    @Override
    public Fields getSchema() {
        return fields;
    }
}
