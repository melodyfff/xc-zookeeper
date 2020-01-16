package com.xinchen.zookeeper.tutorial.simple;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 * 通过zookeeper客户端连接和操作zk
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2020/1/16 22:41
 */
public class ZookeeperSimple {
    static final String HOST = "127.0.0.1:2181";
    /**ms*/
    static final int SESSION_TIME_OUT = 2000;

    static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(">>> try connect to zk.");
        try (ZooKeeper zk = new ZooKeeper(HOST, SESSION_TIME_OUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 获取当前状态
                final Event.KeeperState state = watchedEvent.getState();
                final Event.EventType type = watchedEvent.getType();
                // 成功建立连接
                if (Event.KeeperState.SyncConnected == state){
                    if (Event.EventType.None == type){
                        System.out.println(">>> connection to zk.");
                        latch.countDown();
                    }
                }
            }
        })){
            // 等待连接成功
            latch.await();

            zk.create("/xinchen", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            System.out.println("...");
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
