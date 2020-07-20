package com.xinchen.zookeeper.example.datamonitor;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 *
 * @author xinchen
 * @version 1.0
 * @date 20/07/2020 11:31
 */
public class Executor implements Watcher, Runnable, DataMonitorListener {

    private DataMonitor dm;
    private ZooKeeper zk;


    private String filename;
    private String exec[];

    Process child;

    public Executor(String hostPort, String znode, String filename,
                    String exec[]) throws IOException {
        this.filename = filename;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!dm.dead) {
                try {
                    // 等待被唤醒
                    wait();
                    System.out.println("被唤醒");
                } catch (InterruptedException ignored) {
                    // 忽略中断
                }
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {
        dm.process(event);
    }

    @Override
    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println(Thread.currentThread().getName()+" Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println(Thread.currentThread().getName()+" Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try (FileOutputStream fos = new FileOutputStream(filename)){
                fos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(Thread.currentThread().getName()+" Starting child");
                // 控制进程输出命令
                child = Runtime.getRuntime().exec(exec);
                // 输出至控制台
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

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
            } catch (IOException ignored) {
            }
        }
    }
}
