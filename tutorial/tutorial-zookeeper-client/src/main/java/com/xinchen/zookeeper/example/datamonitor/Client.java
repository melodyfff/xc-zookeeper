package com.xinchen.zookeeper.example.datamonitor;

/**
 *
 * 官网实例： https://github.com/apache/zookeeper/blob/master/zookeeper-docs/src/main/resources/markdown/javaExample.md
 *
 * @author xinchen
 * @version 1.0
 * @date 20/07/2020 14:40
 */
public class Client {
    public static void main(String[] args) {
        String hostPort = "127.0.0.1";
        String znode = "/example-monitor";
        String filename = "/tmp/hello";
        String exec[] = new String[]{"echo","ok"};
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
