package com.xinchen.zookeeper.tutorial.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;


/**
 *
 * PathChildrenCache的一个示例。
 * 示例“harness”是命令处理器允许在路径中添加/更新/删除节点。
 * 当一个更新发生时，PathChildrenCache将保持缓存这些更改和输出。
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/3 23:14
 */
@Slf4j
public class PathCacheExample {
    private static final String PATH = "/example/cache";


    public static void main(String[] args) throws Exception {
        // 模拟测试zookeeper服务
        TestingServer server = new TestingServer();

        CuratorFramework client = null;

        PathChildrenCache cache = null;


        try {
            // 重试策略，当重试时进行休眠
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();

            // cacheData设置为true,节点内容除了stat之外还被缓存
            cache = new PathChildrenCache(client, PATH, true);

            //TODO
            processCommands(client,cache);
        }finally {
            // 关闭连接
            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(server);
        }

    }

    private static void addListener(PathChildrenCache cache){
        // a PathChildrenCacheListener is optional. Here, it's used just to log changes
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()){
                    // 监听添加节点
                    case CHILD_ADDED: {
                        log.info("Node added: {}", ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }
                    // 监听修改节点
                    case CHILD_UPDATED:
                        log.info("Node changed: {}",ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    // 监听删除节点
                    case CHILD_REMOVED:
                        log.info("Node removed: {}",ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    default:
                        break;
                }
            }
        };

        // 将此监听器添加到cache上
        cache.getListenable().addListener(listener);
    }

    private static void processCommands(CuratorFramework client,PathChildrenCache cache){
        // 一个简单的命令行处理器

        // 打印帮助日志
        printHelp();
    }



    private static void printHelp(){
        log.info("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:");
        log.info("set <name> <value>: Adds or updates a node with the given name");
        log.info("remove <name>: Deletes the node with the given name");
        log.info("list: List the nodes/values in the cache");
        log.info("quit: Quit the example");
    }
}
