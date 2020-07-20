package com.xinchen.zookeeper.example.datamonitor;

/**
 *
 * Other classes use the DataMonitor by implementing this method
 *
 * @author xinchen
 * @version 1.0
 * @date 20/07/2020 13:35
 */
public interface DataMonitorListener {
    /**
     * The existence status of the node has changed.
     */
    void exists(byte data[]);

    /**
     * The ZooKeeper session is no longer valid.
     *
     * @param rc
     *                the ZooKeeper reason code
     */
    void closing(int rc);
}
