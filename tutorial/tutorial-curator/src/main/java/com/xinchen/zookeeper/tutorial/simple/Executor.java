package com.xinchen.zookeeper.tutorial.simple;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * 官网实例: https://zookeeper.apache.org/doc/current/javaExample.html
 * 实例解读: https://blog.csdn.net/liyiming2017/article/details/83276706
 *
 * 加入以下参数运行
 * <pre>
 *     :2181 /x data echo reading data from zk
 *
 *     :2181    zk服务器地址
 *     /x       需要监听的路径
 *     data     从zNode获取到的数据保存到本地文件中
 *     echo ok  当监听的路径发生 新增/修改/删除 事件触发的时候执行回调
 * </pre>
 *
 * @author xinchen
 * @version 1.0
 * @date 19/01/2020 13:29
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener{
    String znode;
    DataMonitor dm;
    ZooKeeper zk;
    String filename;
    String[] exec;
    Process child;


    public Executor(String hostPort, String znode, String filename, String[] exec) throws KeeperException, IOException {
        this.filename = filename;
        this.exec = exec;

        // String connectString, int sessionTimeout, Watcher watcher
        zk = new ZooKeeper(hostPort, 3000, this);

        // ZooKeeper zk, String zNode, Watcher chainedWatcher, DataMonitorListener listener
        dm = new DataMonitor(zk, znode, null, this);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err
                    .println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            synchronized (this){
                // 当数据监听没有结束的时候
                while (!dm.dead){
                    // 进入等待
                    wait();
                }
            }
        } catch (InterruptedException e){
            // ignore
        }
    }

    @Override
    public void process(WatchedEvent event) {
        dm.process(event);
    }

    @Override
    public void exists(byte[] data) {
        if (data == null) {
            // 当zNode节点之前存在,并执行过回调
            // 此时delete该节点,停止之前的回调
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            // 之前的回调可能还没执行完
            // 如果再次触发,则停止之前的回调执行,进入等待
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                //保存znode数据至文件
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                // 开始执行回调exec
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                // 输出信息
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closing(int rc) {
        // 以响应ZooKeeper连接永久消失。
        synchronized (this){
            // 唤醒等待线程
            notifyAll();
        }
    }

    /**
     * 写数据线程
     */
    static class StreamWriter extends Thread {
        OutputStream os;

        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }
        @Override
        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
                // ignore
            }

        }
    }
}

class DataMonitor implements Watcher, AsyncCallback.StatCallback{
    private ZooKeeper zk;
    private String zNode;
    private Watcher chainedWatcher;
    boolean dead;
    private DataMonitorListener listener;
    private byte[] prevData;


    public DataMonitor(ZooKeeper zk, String zNode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zk = zk;
        this.zNode = zNode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // 通过检查节点是否存在来开始工作。完全由事件驱动
        // path,watch,StatCallback,object
        zk.exists(zNode, true, this, null);
    }

    /**
     * 在这个函数中，当DataMonitor获得一个znode的事件时，它会调用ZooKeeper.exists（）来找出发生了什么变化
     * @param event event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // 当connection changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with server
                    // any watches triggered while the client was disconnected will be delivered (按顺序)
                    System.out.println(">>> connection to zk.");
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.intValue());
                    break;
                default:
                    break;
            }
        } else {
            if (path != null && path.equals(zNode)) {
                // Something has changed on the node, let's find out
                // String path, boolean watch, StatCallback cb, Object ctx
                zk.exists(zNode, true, this, null);
            }
        }

        // 监听链
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }


    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.SessionExpired:
            case KeeperException.Code.NoAuth:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zk.exists(zNode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                // String path, boolean watch, Stat stat
                b = zk.getData(zNode, false, null);
            } catch (KeeperException e) {
                // 不用担心recovering
                // watch回调被任何异常触发
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }

        if ((b == null && b != prevData) || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }
    /**
     * Other classes use the DataMonitor by implementing this method
     */
    public interface DataMonitorListener{
        /**
         * 节点存在的状态变更
         * 主要是在异步状态回调的时候被调用
         * @param data 节点数据
         */
        void exists(byte[] data);

        /**
         * zookeeper session 失效
         * @param rc the ZooKeeper reason cod
         */
        void closing(int rc);
    }
}
