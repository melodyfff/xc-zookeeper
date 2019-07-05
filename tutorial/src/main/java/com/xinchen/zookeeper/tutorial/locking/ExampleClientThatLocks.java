package com.xinchen.zookeeper.tutorial.locking;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * @author xinchen
 * @version 1.0
 * @date 05/07/2019 12:39
 */
@Slf4j
public class ExampleClientThatLocks {
    /**
     * 可跨JVM的互斥可重入锁
     *
     * 使用Zookeeper来保持锁定
     *
     * 所有JVM进程需要注意使用相同的锁路径
     *
     * 此外,这个互斥锁是'公平锁',每个用户将按请求的顺序获取互斥锁(从ZK视角看)
     */
    private final InterProcessMutex lock;


    private final FakeLimitedResource resource;

    private final String clientName;

    public ExampleClientThatLocks(CuratorFramework client,String lockPath,FakeLimitedResource resource,String clientName){
        this.resource = resource;
        this.clientName = clientName;
        lock = new InterProcessMutex(client, lockPath);
    }

    public void doWork(long time, TimeUnit unit) throws Exception {

        // 获取互斥锁 - 阻塞直到它可用或给定时间到期。 注意：同一个线可以重新调用获取
        if (!lock.acquire(time,unit)){
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }

        try {
            log.info("{} has the lock",clientName);
            resource.use();
        }finally {
            log.info("{} releasing the lock",clientName);
            // always release the lock in a finally block
            lock.release();
        }
    }

}
