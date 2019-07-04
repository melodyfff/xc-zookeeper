package com.xinchen.zookeeper.tutorial.cache;

import com.xinchen.zookeeper.tutorial.framework.CreateClientExamples;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * 使用zk路径下所有节点数据作为缓存
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/5 0:43
 */
@Slf4j
public class TreeCacheExample {

    private static final String PATH = "/tree/cache";


    public static void main(String[] args) throws Exception {
        // 创建客户端
        // 此处也可以连接真实环境
        TestingServer testingServer = new TestingServer();
        CuratorFramework client = CreateClientExamples.createSimple(testingServer.getConnectString());



        // 绑定未处理异常监听器
        client.getUnhandledErrorListenable().addListener(((message, e) -> {
            log.error("error={} , exception: {}", message, e);
        }));

        // 绑定状态连接监听器
        client.getConnectionStateListenable().addListener(((c, newState) -> {
            log.info("state={}", newState);
        }));

        // 启动客户端
        client.start();

        // 新建treeCache,设置是否缓存每个节点的字节数据; 默认{@code true}。
        TreeCache cache = TreeCache.newBuilder(client, "/").setCacheData(false).build();

        // 设置监听器，判断是否添加成功
        cache.getListenable().addListener(((c, event) -> {
            if (null != event.getData()) {
                log.info("type={} path={}", event.getType(), event.getData().getPath());
            } else {
                log.info("type={}", event.getType());
            }
        }));

        // 启动缓存
        cache.start();


        // 初始化数据
        initData(client);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        in.readLine();
    }


    public static void initData(CuratorFramework client) throws Exception {
        // 仅仅模拟数据
        for (int i = 0;i<10;i++){
            client.create().creatingParentContainersIfNeeded().forPath(ZKPaths.makePath(PATH, String.valueOf(i)), "ok".getBytes());
        }
    }
}
