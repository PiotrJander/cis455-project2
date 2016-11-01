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

    /**
     * Called when a task for this component is initialized within a
     * worker on the cluster. It provides the spout with the environment
     * in which the spout executes.
     *
     * @param config    The Storm configuration for this spout. This is
     *                  the configuration provided to the topology merged in
     *                  with cluster configuration on this machine.
     * @param topo
     * @param collector The collector is used to emit tuples from
     *                  this spout. Tuples can be emitted at any time, including
     *                  the open and close methods. The collector is thread-safe
     *                  and should be saved as an instance variable of this spout
     */
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

    /**
     * Called when an ISpout is going to be shutdown.
     * There is no guarantee that close will be called, because the
     * supervisor kill -9’s worker processes on the cluster.
     */
    @Override
    public void close() {
        // TODO
    }

    /**
     * When this method is called, Storm is requesting that the Spout emit
     * tuples to the output collector. This method should be non-blocking,
     * so if the Spout has no tuples to emit, this method should return.
     */
    @Override
    public void nextTuple() {
        // TODO
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }
}
