package com.xinchen.zookeeper.tutorial.cache;

import com.google.common.collect.Lists;
import com.xinchen.zookeeper.tutorial.discovery.ExampleServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


/**
 *
 * PathChildrenCache的一个示例。
 * 示例“harness”是命令处理器允许在路径中添加/更新/删除节点。
 * 当一个更新发生时，PathChildrenCache将保持缓存这些更改和输出。
 *
 * {@link ExampleServer} 服务实例
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
            cache.start();

            //命令行执行
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

    private static void processCommands(CuratorFramework client,PathChildrenCache cache) throws Exception {
        // 一个简单的命令行处理器

        // 打印帮助日志
        printHelp();


        // 服务列表
        List<ExampleServer> servers = Lists.newArrayList();

        try {
            // 添加监听器
            addListener(cache);

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done = false;
            while (!done){
                System.out.println("> ");
                String line = in.readLine();

                if (null == line){
                    break;
                }

                // 提取命令和参数
                String command = line.trim();
                String[] parts = command.split("\\s");

                if (0 == parts.length){
                    continue;
                }

                String operation = parts[0];
                String args[] = Arrays.copyOfRange(parts, 1, parts.length);

                if("help".equalsIgnoreCase(operation)||"?".equalsIgnoreCase(operation)){
                    printHelp();
                } else if ("q".equalsIgnoreCase(operation) || "quit".equalsIgnoreCase(operation)) {
                    done = true;
                } else if("set".equals(operation)){
                    setValue(client,command,args);
                } else if ("remove".equals(operation)){
                    remove(client,command,args);
                }else if ("list".equals(operation)||"ls".equals(operation)){
                    list(cache);
                }

                // 只是为了让控制台输出赶上来
                Thread.sleep(1000);
            }
        }finally {
            for (ExampleServer server:servers){
                CloseableUtils.closeQuietly(server);
            }
        }
    }


    private static void list(PathChildrenCache cache){
        // 列出缓存中的数据
        if (0==cache.getCurrentData().size()){
            log.info("* empty *");
        } else {
            for (ChildData data:cache.getCurrentData()) {
                log.info("{}={}",data.getPath(),new String(data.getData()));
            }
        }
    }

    private static void remove(CuratorFramework client,String command,String[] args) throws Exception {
        if (1!=args.length){
            log.error("syntax error (expected remove <path>): {}",command);
            return;
        }

        String name = args[0];
        String path = ZKPaths.makePath(PATH, name);
        try {
            // 删除节点
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e){
            // ignore
        }
    }

    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception {
        if (2!=args.length){
            log.error("syntax error (expected set <path> <value>): {}",command);
            return;
        }


        String name = args[0];
        if(name.contains("/")){
            log.error("Invalid node name {}",name);
            return;
        }

        // 获取路径
        String path = ZKPaths.makePath(PATH, name);
        byte[] bytes = args[1].getBytes();

        try {
            // 设置值
            client.setData().forPath(path, bytes);
        } catch (KeeperException.NoNodeException e){
//            log.warn("No NODE,WILL CREATE.");
            // 如果没有这个路径则新建父节点后再存进去
            client.create().creatingParentContainersIfNeeded().forPath(path, bytes);
        }
    }


    private static void printHelp(){
        log.info("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:");
        log.info("set <name> <value>: Adds or updates a node with the given name");
        log.info("remove <name>: Deletes the node with the given name");
        log.info("list: List the nodes/values in the cache");
        log.info("quit: Quit the example");
    }
}
