package com.xinchen.zookeeper.tutorial.leader;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 *
 * @author xinchen
 * @version 1.0
 * @date 03/07/2019 15:43
 */
@Slf4j
public class LeaderSelectorExample {

    private static final int CLIENT_QTY = 10;
    private static final String PATH = "/example/leader";


    public static void main(String[] args) throws Exception {
        log.info("创建 [{}] 个客户端,在领导人选举发生前,让每个人都进行领导谈判，然后等待一个随机的秒数");
        log.info("请注意，领导者选举是公平的：所有客户都将成为领导者并且将这样做的次数相同");

        List<CuratorFramework> clients = Lists.newArrayList();

        List<ExampleClient> examples = Lists.newArrayList();

        TestingServer server = new TestingServer();

        try {
            for (int i = 0; i < CLIENT_QTY; i++) {
                // ExponentialBackoffRetry 重试策略(重试之间增加休眠时间,重试的次数)
                CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                clients.add(client);

                ExampleClient example = new ExampleClient(client, PATH, "Client #" + i);
                examples.add(example);

                // 开启选举
                client.start();
                example.start();
            }

            log.info("Press enter/return to quit");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            log.info("Shutting down...");
            for (ExampleClient exampleClient:examples){
                CloseableUtils.closeQuietly(exampleClient);
            }
            for (CuratorFramework client:clients){
                CloseableUtils.closeQuietly(client);
            }

            CloseableUtils.closeQuietly(server);
        }
    }
}
