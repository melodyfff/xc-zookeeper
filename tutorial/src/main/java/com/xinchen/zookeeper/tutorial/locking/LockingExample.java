package com.xinchen.zookeeper.tutorial.locking;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xinchen
 * @version 1.0
 * @date 05/07/2019 13:39
 */
@Slf4j
public class LockingExample {

    /** 线程数 */
    private static final int QTY = 5;

    /** 任务执行次数 */
    private static final int REPETITIONS = QTY * 10;

    private static final String PATH = "/examples/locks";

    public static void main(String[] args) throws Exception {
        // all of the useful sample code is in ExampleClientThatLocks.java

        // 模拟一些一次只能一个线程访问的共享资源
        final FakeLimitedResource resource = new FakeLimitedResource();

        // 线程池
        ExecutorService service = Executors.newFixedThreadPool(QTY);

        final TestingServer server = new TestingServer();

        try {
            for (int i = 0;i<QTY;++i){
                final int index = i;
                Callable<Void> task = () -> {
                    CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                    try {
                        client.start();
                        ExampleClientThatLocks example = new ExampleClientThatLocks(client, PATH, resource, "Client" + index);
                        for (int j = 0; j < REPETITIONS; ++j) {
                            example.doWork(10, TimeUnit.SECONDS);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error("{}", e);
                    } finally {
                        CloseableUtils.closeQuietly(client);
                    }
                    return null;
                };

                service.submit(task);
            }

            // 将线程池状态置为STOP。企图立即停止，事实上不一定
            service.shutdown();

            // 当前线程阻塞，直到所有已经提交的任务执行完毕\等待超时\中断,以先发生的事件为主
            service.awaitTermination(10, TimeUnit.MINUTES);
        } finally {
            CloseableUtils.closeQuietly(server);

        }

    }
}
