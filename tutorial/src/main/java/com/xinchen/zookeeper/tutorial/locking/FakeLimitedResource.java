package com.xinchen.zookeeper.tutorial.locking;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Simulates some external resource that can only be access by one process at a time
 *
 * 模拟一次只能由一个进程访问的外部资源
 *
 *
 * @author xinchen
 * @version 1.0
 * @date 05/07/2019 12:55
 */

public class FakeLimitedResource {

    private final AtomicBoolean inUse = new AtomicBoolean(false);

    public void use() throws InterruptedException {

        // 在真实应用中,这将访问/操作 '共享资源'
        // in a real application this would be accessing/manipulating a shared resource


        // 尝试去替换inUse,期待此时inUse值为false替换为true
        // 替换成功返回true
        // 替换失败返回false
        if (!inUse.compareAndSet(false,true)){
            throw new IllegalStateException("Needs to be used by one client at a time");
        }

        try {
            // 考虑模拟资源进行处理,方便观察
            Thread.sleep((long)(2 * 200));
        } finally {
            inUse.set(false);
        }

    }

}

