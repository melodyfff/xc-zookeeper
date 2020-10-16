package com.xinchen.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 打印 znode path {@link WatchedEvent}事件信息
 *
 * @author xinchen
 * @version 1.0
 * @date 16/10/2020 14:39
 */
public class SimpleWatcher implements Watcher {

    private final static Logger log = Logger.getLogger(SimpleWatcher.class.getName());

    @Override
    public void process(WatchedEvent event) {
        log.log(Level.INFO,String.format("%s - %s - %s",Thread.currentThread().getName(),event.getPath(),event.toString()));
    }
}
