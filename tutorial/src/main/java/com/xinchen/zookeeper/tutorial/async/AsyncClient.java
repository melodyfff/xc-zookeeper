package com.xinchen.zookeeper.tutorial.async;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.CountDownLatch;

/**
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/6 23:00
 */
public class AsyncClient {

    private static final String PATH = "/example/async";

    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 仅仅用户测试
        TestingServer server = new TestingServer();

        final CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.244.128:2181", new ExponentialBackoffRetry(1000, 3));

        client.start();
        Thread.sleep(1000);
        try {

            // 创建本次测试根节点
            client.create().creatingParentsIfNeeded().forPath(PATH);

            // 异步创建zNode
            // 可在ZK服务端 ls /example/async 查看
            AsyncExamples.create(client,PATH+"/create","hello".getBytes());

            // 异步创建zNode , 并绑定监听
            // 可在ZK服务端 set /example/async/watch1 test 查看效果
            AsyncExamples.createThenWatch(client,PATH+"/watch1");

            // 异步创建zNode , 添加数据 ， 并绑定监听
            AsyncExamples.createThenWatchSimple(client,PATH+"/watch2","hello".getBytes());


            latch.await();


        } finally {
            latch.countDown();
            CloseableUtils.closeQuietly(client);
        }


    }

}
