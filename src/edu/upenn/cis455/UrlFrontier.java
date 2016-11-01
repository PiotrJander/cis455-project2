package edu.upenn.cis455;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public enum UrlFrontier {

    INSTANCE;

    private final ConcurrentLinkedQueue<URL> queue = new ConcurrentLinkedQueue<>();

    UrlFrontier() {
        // TODO maybe init here
    }

    public void addAll(List<URL> urls) {
        queue.addAll(urls);
    }

    public URL poll() {
        return queue.poll();
    }
}
