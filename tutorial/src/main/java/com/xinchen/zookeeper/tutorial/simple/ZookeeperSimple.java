package com.xinchen.zookeeper.tutorial.simple;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
 * 可以通过以下操作添加Watch:
 * <pre>
 *     1) exits      : 可以监听到节点创建、节点的内容修改、节点的删除
 *     2) getData    : 可以监听节点内容修改，节点的删除
 *     3) getChildren: 可以监听子节点的添加、删除（子节点内容变化和子节点的子节点的变化不能监听）
 * </pre>
 *
 *
 * 有关zk的Watch主要有以下特性:
 * <pre>
 *
 *
 *     1) 一次性: 无论是服务端还是客户端，一旦一个 Watcher 被触发，ZooKeeper 都会将其从相应的存储中移除。
 *            因此，在 Watcher 的使用上，需要反复注册。这样的设计有效地减轻了服务端的压力。
 *
 *
 *     2) 轻量级: WatcherEvent是 ZooKeeper 整个 Watcher 通知机制的最小通知单元 ,这个数据结构中只包含三部分内容：通知状态、事件类型和节点路径。
 *               其实就是本地JVM的Callback
 *
 * </pre>
 *
 *
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2020/1/16 22:41
 */
public class ZookeeperSimple {
    private static final String HOST = "127.0.0.1:2181";
    /**
     * ms
     */
    private static final int SESSION_TIME_OUT = 2000;

    static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(">>> try connect to zk.");
        ZooKeeper zk = new ZookeeperMaker().getZk();
        try {
            // 等待连接成功
            latch.await();


            // 判断节点是否存在,不存在则新建
            if (null == zk.exists("/xinchen", true)) {
                zk.create("/xinchen", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                // 和 setAcl /xinchen word:anyone:crwda 修改所有人拥有所有权限
                zk.create("/xinchen/hello", "world".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);



                // 获取节点下的数据
                Stat stat = new Stat();
                System.out.println(stat);
                System.out.println(new String(zk.getData("/xinchen", true, stat)));
                System.out.println(stat);
            }

            // 创建临时节点
            zk.create("/xinchen/tmp", "tmp".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name) {
                    System.out.println(String.format("rc=%s path=%s ctx=%s name=%s",KeeperException.Code.get(rc),path,ctx,name));
                }
            },"环境上下文");
            // 添加默认的的zk客户端watch
            zk.exists("/xinchen/tmp", true);


            // 创建临时顺序节点,未添加watch
            zk.create("/xinchen/tmp-", "tmp".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.Create2Callback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name, Stat stat) {
                    System.out.println(String.format("rc=%s path=%s ctx=%s name=%s stat=%s",KeeperException.Code.get(rc),path,ctx,name,stat));
                }
            },"环境上下文");


            // 获取子节点
            final List<String> children = zk.getChildren("/xinchen/hello", true);
            System.out.println(">>> /xinchen childrens: "+children);

            System.out.println("...");

            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * zk 客户端注册的 Watcher
     * 这个Watcher将作为整个ZooKeeper会话期间的默认Watcher，会一直被保存在客户端ZKWatchManager的defaultWatcher中
     *
     * 另外，ZooKeeper 客户端也可以通过 getData、exists 和 getChildren 三个接口来向 ZooKeeper 服务器注册 Watcher
     *
     * @see ZooKeeper.ZKWatchManager#defaultWatcher
     */
    static class ZookeeperMaker implements Watcher {
        DefaultDataMonitor dm;
        ZooKeeper zk;

        public ZookeeperMaker() throws IOException {
            zk = new ZooKeeper(HOST, SESSION_TIME_OUT, this);
            dm = new DefaultDataMonitor(zk);
        }

        @Override
        public void process(WatchedEvent event) {
           dm.process(event);
        }

        public ZooKeeper getZk() {
            return zk;
        }
    }


    /**
     * 同时实现{@link Watcher}和{@link org.apache.zookeeper.AsyncCallback.StatCallback}的监听器
     */
    static class DefaultDataMonitor implements Watcher, AsyncCallback.StatCallback{
        ZooKeeper zk;

        public DefaultDataMonitor(ZooKeeper zk) {
            this.zk = zk;
        }

        @Override
        public void process(WatchedEvent event) {
            // 获取当前状态
            final Event.KeeperState state = event.getState();
            final Event.EventType type = event.getType();

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
                        System.out.println(">>> ");
                        break;
                }
            } else {
                if (null!=event.getPath()){
                    try {
                        // 再次加上监听器,主要是由于watch只能使用一次,这里循环添加监听
                        final Stat exists = zk.exists(event.getPath(), true);
                        // 针对子节点循环新增监听器
                        if (type== Event.EventType.NodeChildrenChanged && null!=exists && exists.getNumChildren()!=0){
                            zk.getChildren(event.getPath(), true);
                        }

                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(">>> zk path ["+event.getPath()+"] state change State: "+ state + " Type: " + type);
            }
        }

        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            System.out.println(String.format("rc=%s path=%s ctx=%s stat=%s",KeeperException.Code.get(rc),path,ctx,stat));
        }
    }
}
