package com.xinchen.zookeeper.tutorial.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.Closeable;
import java.io.IOException;

/**
 * 服务发现(service discovery)注册实例的简单实现
 *
 * 分布式应用集群将创建和{@link ExampleServer}类似的实例
 *
 * 在应用程序启动时启动它，并在应用程序关闭时关闭它。
 *
 * @author xinchen
 * @version 1.0
 * @date 04/07/2019 08:56
 */
public class ExampleServer implements Closeable {

    private final ServiceDiscovery<InstanceDetails> serviceDiscovery;

    /** 代表服务实例的POJO */
    private final ServiceInstance<InstanceDetails> thisInstance;


    public ExampleServer(CuratorFramework client,String path,String serviceName,String description) throws Exception {
        // An abstraction for specifying a URI for an instance allowing for variable substitutions.
        // in a real application, you'd have a convention of some kind for the URI layout
        UriSpec uriSpec = new UriSpec("{scheme}://foo.com:{port}");

        this.thisInstance = ServiceInstance.<InstanceDetails>builder()
                .name(serviceName)
                .payload(new InstanceDetails(description))
                .port((int) (65535 * Math.random()))
                .uriSpec(uriSpec)
                .build();

        // json序列化
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<>(InstanceDetails.class);


        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(client)
                .basePath(path)
                .serializer(serializer)
                .thisInstance(thisInstance)
                .build();
    }

    public ServiceInstance<InstanceDetails> getThisInstance(){
        return thisInstance;
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}
