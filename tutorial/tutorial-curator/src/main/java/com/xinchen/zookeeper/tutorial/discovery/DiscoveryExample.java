package com.xinchen.zookeeper.tutorial.discovery;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 启动和运行实例
 *
 * @author xinchen
 * @version 1.0
 * @date 04/07/2019 09:24
 */
@Slf4j
public class DiscoveryExample {
    private static final String PATH = "/discovery/example";

    public static void main(String[] args) throws Exception {
        TestingServer server = new TestingServer();
        CuratorFramework client = null;

        ServiceDiscovery<InstanceDetails> serviceDiscovery = null;
        Map<String, ServiceProvider<InstanceDetails>> providers = Maps.newHashMap();


        try {
            // 定义连接地址和重试策略
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            // 启动服务
            client.start();

            JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);
            serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                    .client(client)
                    .basePath(PATH)
                    .serializer(serializer)
                    .build();


            // 开启服务发现
            serviceDiscovery.start();


            // 执行命令
            processCommands(serviceDiscovery, providers, client);

        } finally {
            for (ServiceProvider<InstanceDetails> cache : providers.values()) {
                CloseableUtils.closeQuietly(cache);
            }
            CloseableUtils.closeQuietly(serviceDiscovery);
            CloseableUtils.closeQuietly(client);
            CloseableUtils.closeQuietly(server);
        }

    }

    private static void processCommands(ServiceDiscovery<InstanceDetails> serviceDiscovery, Map<String, ServiceProvider<InstanceDetails>> providers, CuratorFramework client) throws Exception {
        // 命令处理器
        printHelp();

        List<ExampleServer> servers = Lists.newArrayList();
        try {
            // 接收终端输入命令
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done = false;

            while (!done) {
                System.out.print("> ");
                String line = in.readLine();

                if (null == line) {
                    break;
                }

                // 从命令中提取参数args[]
                String command = line.trim();
                String[] parts = command.split("\\s");
                if (0 == parts.length) {
                    continue;
                }


                String operation = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                // 指令合集
                if ("help".equalsIgnoreCase(operation) || "?".equalsIgnoreCase(operation)) {
                    printHelp();
                } else if ("q".equalsIgnoreCase(operation) || "quit".equalsIgnoreCase(operation)) {
                    done = true;
                } else if ("add".equals(operation)) {
                    addInstance(args, client, command, servers);
                } else if ("delete".equals(operation)) {
                    deleteInstance(args, command, servers);
                } else if ("random".equals(operation)) {
                    listRandomInstance(args, serviceDiscovery, providers, command);
                } else if ("list".equals(operation) || "ls".equals(operation)) {
                    listInstances(serviceDiscovery);
                }

            }
        } finally {
            for (ExampleServer server : servers) {
                CloseableUtils.closeQuietly(server);
            }
        }
    }


    private static void listRandomInstance(String[] args, ServiceDiscovery<InstanceDetails> serviceDiscovery, Map<String, ServiceProvider<InstanceDetails>> providers, String command) throws Exception {
        // 使用ServiceProvider的例子
        // 在实际场景中需要提前为service(s)创建ServiceProvider

        // 检测命令
        if (1 != args.length) {
            log.error("syntax error (expected random <name>): {}", command);
            return;
        }

        // 获取服务
        String serviceName = args[0];
        ServiceProvider<InstanceDetails> provider = providers.get(serviceName);

        if (null == provider) {
            // 如果没有则注册该服务,策略为RandomStrategy(此策略始终从列表中选择一个随机实例)
            provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceName).providerStrategy(new RandomStrategy<>()).build();
            providers.put(serviceName, provider);

            // 启动
            provider.start();

            // 给提供者时间预热 - 在实际应用程序中，您不需要这样做
            // give the provider time to warm up - in a real application you wouldn't need to do this
            Thread.sleep(2500);
        }

        // 获取服务实例
        ServiceInstance<InstanceDetails> instance = provider.getInstance();
        if (null == instance) {
            log.error("No instance named : {}", serviceName);
        } else {
            outputInstance(instance);
        }
    }


    private static void listInstances(ServiceDiscovery<InstanceDetails> serviceDiscovery) throws Exception {
        // 这显示了如何查询服务发现中的所有实例
        try {
            // Return the names of all known services
            Collection<String> serviceNames = serviceDiscovery.queryForNames();
            log.info("{} type(s)", serviceNames.size());

            for (String serviceName : serviceNames) {
                log.info(serviceName);

                // Return all known instances for the given service
                Collection<ServiceInstance<InstanceDetails>> instances = serviceDiscovery.queryForInstances(serviceName);

                for (ServiceInstance<InstanceDetails> instance : instances) {
                    // 打印实例详情
                    outputInstance(instance);
                }
            }
        } finally {
            // 官网例子中只要执行过list指令后就会关闭ServiceDiscovery,这样会导致正常q/quit退出的时候报错
            // CloseableUtils.closeQuietly(serviceDiscovery);
        }
    }

    private static void addInstance(String[] args, CuratorFramework client, String command, List<ExampleServer> servers) throws Exception {
        // 模拟一个instance实例的发布
        // 在实际应用程序中，这将是一个单独的过程

        if (2 > args.length) {
            log.error("syntax error (expected add <name> <description>): {}", command);
            return;
        }

        // 拼接描述信息
        StringBuilder description = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                description.append(' ');
            }
            description.append(args[i]);
        }

        // 添加服务
        String serviceName = args[0];
        ExampleServer server = new ExampleServer(client, PATH, serviceName, description.toString());
        servers.add(server);
        server.start();
        log.info("{} added", serviceName);
    }

    private static void deleteInstance(String[] args, String command, List<ExampleServer> servers) throws IOException {
        // 模拟关闭一个随机实例
        // 在实际应用程序中，这会因为正常操作，崩溃，维护等而发生。

        if (1 != args.length) {
            log.error("syntax error (expected delete <name>): {}", command);
            return;
        }

        // 从服务列表中查询服务,没有则返回null
        final String serviceName = args[0];
        ExampleServer server = servers.stream().filter((serverInstance) -> serverInstance.getThisInstance().getName().endsWith(serviceName)).findFirst().orElse(null);


        if (null == server) {
            log.error("No servers found named: {}", serviceName);
            return;
        }

        //移除服务
        servers.remove(server);

        CloseableUtils.closeQuietly(server);
        log.info("Removed a random instance of: {}", serviceName);

    }

    private static void outputInstance(ServiceInstance<InstanceDetails> instance) {
        //有效载荷:URI
        log.info("  {}:{}", instance.getPayload().getDescription(), instance.buildUriSpec());
    }

    private static void printHelp() {
        log.info("An example of using the ServiceDiscovery APIs. This example is driven by entering commands at the prompt:");
        log.info("add <name> <description>: Adds a mock service with the given name and description");
        log.info("delete <name>: Deletes one of the mock services with the given name");
        log.info("list: Lists all the currently registered services");
        log.info("random <name>: Lists a random instance of the service with the given name");
        log.info("quit: Quit the example");
    }

}
