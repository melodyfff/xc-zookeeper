package com.xinchen.zookeeper.tutorial.simple;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 通过zookeeper客户端连接和操作zk
 * <p>
 *     <pre>
 *        CREATE    c   可以创建子节点
 * 　　   DELETE   d   可以删除子节点（仅下一级节点）
 * 　　   READ     r   可以读取节点数据及显示子节点列表
 * 　　   WRITE    w   可以设置节点数据
 * 　　   ADMIN    a   可以设置节点访问控制列表权限
 *     </pre>
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2020/1/16 22:41
 */
public class ZookeeperSimple {
    static final String HOST = "127.0.0.1:2181";
    /**
     * ms
     */
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

                if (Event.EventType.None == type) {
                    // 成功建立连接
                    switch (state) {
                        case AuthFailed:
                            System.out.println(">>> AuthFailed to zk.");
                            break;
                        case SyncConnected:
                            System.out.println(">>> connection to zk.");
                            latch.countDown();
                            break;
                        case Disconnected:
                            System.out.println(">>> zk disconnected.");
                            break;
                        case Expired:
                            System.out.println(">>> zk expired.");
                            break;
                        default:
                            break;
                    }
                } else {
                    System.out.println(">>> zk state change- State: "+ state + " Type: " + type);
                }
            }
        })) {
            // 等待连接成功
            latch.await();

            // 判断节点是否存在,不存在则新建
            if (null == zk.exists("/xinchen", false)) {
                zk.create("/xinchen", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                // 和 setAcl /xinchen word:anyone:crwda 修改所有人拥有所有权限
                zk.create("/xinchen/hello", "world".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // 创建临时节点
            zk.create("/xinchen/tmp", "tmp".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name) {
                    System.out.println(String.format("rc=%s path=%s ctx=%s name=%s",KeeperException.Code.get(rc),path,ctx,name));
                }
            },"环境上下文");

            // 创建临时顺序节点
            zk.create("/xinchen/tmp-", "tmp".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.Create2Callback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name, Stat stat) {
                    System.out.println(String.format("rc=%s path=%s ctx=%s name=%s stat=%s",KeeperException.Code.get(rc),path,ctx,name,stat));
                }
            },"环境上下文");

            System.out.println("...");

            TimeUnit.SECONDS.sleep(100);
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
