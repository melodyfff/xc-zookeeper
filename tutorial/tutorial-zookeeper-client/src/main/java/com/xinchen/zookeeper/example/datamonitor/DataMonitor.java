package com.xinchen.zookeeper.example.datamonitor;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

/**
 * A simple class that monitors the data and existence of a ZooKeeper
 * node. It uses asynchronous ZooKeeper APIs.
 *
 * @author xinchen
 * @version 1.0
 * @date 20/07/2020 13:34
 */
public class DataMonitor implements Watcher, AsyncCallback.StatCallback {
    ZooKeeper zk;
    String znode;
    Watcher chainedWatcher;
    DataMonitorListener listener;

    boolean dead;
    /** 存储前一次节点中的数据 */
    byte prevData[];

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher, DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // 检查znode是否符合规范
        // 绑定watcher , 使用watchManager.defaultWatcher
        // path,watch,StatCallback,ctx
        zk.exists(znode, true, this, null);
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.format(Thread.currentThread().getName()+" handle event : %s %n", event.toString());
        final String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // the connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // 本例子中不需要做什么
                    // watchers自动re-registered
                    // 并且当client断开连接时，watcher triggered 并且会传递给下一个
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.intValue());
                default:
                    break;
            }
        } else {
            if (null != path && path.equals(znode)) {
                // re-registered watcher
                zk.exists(znode, true, this, null);
            }
        }

        if (null != chainedWatcher) {
            // deliver
            chainedWatcher.process(event);
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 回调函数处理本地存储的值

        // rc: The return code or the result of the call. see {@link KeeperException.Code}
        // path: The path that we passed to asynchronous calls
        // ctx: Whatever context object that we passed to asynchronous calls
        // stat: {@link org.apache.zookeeper.data.Stat} object of the node on given path.

        boolean exsits;
        switch (rc){
            // KeeperException.Code.OK
            case 0:
                exsits = true;
                break;
            //  KeeperException.Code.NoNode
            case -101:
                exsits = false;
                break;
            //  KeeperException.Code.SessionExpired
            case -112:
            //  KeeperException.Code.NoAuth
            case -102:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte[] b = null;
        if (exsits){
            try {
                b = zk.getData(znode, false, null);
            } catch (InterruptedException ignored) {
                return;
            } catch (KeeperException e) {
                // 此时没必要担心恢复，watch callbacks 将会开始 exception handling
                e.printStackTrace();
            }
        }

        // 和prevData比较是否发生过变动
        if ((null==b && b!=prevData) || (null!=b && !Arrays.equals(prevData,b))){
            // 表面节点状态已经发生改变
            listener.exists(b);
            prevData = b;
        }
    }

}
