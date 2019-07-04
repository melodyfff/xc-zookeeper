package com.xinchen.zookeeper.tutorial.framework;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 *
 * 创建client的例子
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/4 23:54
 */
public class CreateClientExamples {

    public static CuratorFramework createSimple(String connectionString){
        // 重试策略
        // 重试规则如下:
        // retry will wait 1 second - the second will wait up to 2 seconds - the third will wait up to 4 seconds.
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 返回CuratorFramework实例
        return CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
    }

    public static CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy,
                                                     int connectionTimeoutMs, int sessionTimeoutMs){
        return CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }
}
