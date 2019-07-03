package com.xinchen.zookeeper.tutorial.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author xinchen
 * @version 1.0
 * @date 03/07/2019 16:08
 */
@Slf4j
public class ExampleClient extends LeaderSelectorListenerAdapter implements Closeable {

    private final String name;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();

    public ExampleClient(CuratorFramework client,String path,String name){
        this.name = name;

        // create a leader selector using the given path for management
        // all participants(参与者) in a given leader selection must use the same path
        // ExampleClient here is also a LeaderSelectorListener but this isn't required
        leaderSelector = new LeaderSelector(client, path, this);


        // 对于大多数情况，你会希望你的实例在放弃领导时重新排队
        leaderSelector.autoRequeue();
    }


    public void start(){
        // the selection for this instance doesn't start until the leader selector is started
        // leader selection is done in the background so this call to leaderSelector.start() returns immediately
        leaderSelector.start();
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        // 现在是leader,在放弃(relinquishing)领导权前不返还
        final int waitSeconds = (int)(5 * Math.random()) + 1;
        log.info("{} is now the leader. Waiting {} seconds...",name,waitSeconds);
        log.info("{} has been leader {} time(s) before.",name,leaderCount.getAndIncrement());

        try {
            // 模拟执行leadership,等待随机秒
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } catch (InterruptedException e){
            log.error("{} was interrupted.",name);
            // 关闭线程
            Thread.currentThread().interrupt();
        } finally {
            log.info("{} relinquishing leadership.",name);
        }
    }

    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }
}
