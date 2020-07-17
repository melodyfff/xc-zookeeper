package com.xinchen.zookeeper.tutorial.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.AsyncEventException;
import org.apache.curator.x.async.WatchMode;
import org.apache.zookeeper.WatchedEvent;

import java.util.concurrent.CompletionStage;

/**
 * 使用异步DSL的示例
 * <p>
 * Examples using the asynchronous DSL
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/6 22:31
 */
@Slf4j
public class AsyncExamples {
    public static AsyncCuratorFramework wrap(CuratorFramework client) {
        // 包装一个CuratorFramework实例，以便它可以用作异步。
        // 执行一次并重新使用返回的AsyncCuratorFramework实例
        return AsyncCuratorFramework.wrap(client);
    }

    public static void create(CuratorFramework client, String path, byte[] payload) {
        // 通常你会在应用程序的早期包装并重用实例
        AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

        async.create().forPath(path, payload).whenComplete((name, exception) -> {
            // 如果存在异常
            if (null != exception) {
                log.error("AsyncEventException : {}",exception);
            } else {
                log.info("Created node name is {}", name);
            }
        });
    }

    public static void createThenWatch(CuratorFramework client, String path) {
        // 此示例显示异步使用观察者(watchers)观察触发事件和连接问题
        //如果您不需要通知连接问题，使用createThenWatchSimple（）中显示的更简单的方法


        // normally you'd wrap early in your app and reuse the instance
        AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

        //异步在指定路径创建负载，然后观察这个被创建的节点
        async.create().forPath(path).whenComplete((name, exception) -> {
            if (null != exception) {
                // 创建时发生的错误
                log.error("AsyncEventException : {}",exception);
            } else {
                handleWatchedStage(async.watched().checkExists().forPath(path).event());
            }
        });

    }


    public static void createThenWatchSimple(CuratorFramework client,String path,byte[] payload){
        final AsyncCuratorFramework async = wrap(client);


        // 异步在指定路径创建节点存入负载，并监听
        async.create().forPath(path,payload).whenComplete((name, exception) -> {
            if (null != exception){
                // there was a problem creating the node.
                log.error("AsyncEventException : {}",exception);
            } else {
                // 因为“WatchMode.successOnly”只有当EventType是一个节点事件时才会触发
                async.with(WatchMode.successOnly).watched().checkExists().forPath(path).event().thenAccept(watchedEvent -> {
                    log.warn(watchedEvent.getType().toString());
                    log.warn(watchedEvent.toString());
                });
            }
        });

    }


    private static void handleWatchedStage(CompletionStage<WatchedEvent> watchedStage){
        // 因为观察者可以被多次触发，并且CompletionStage不支持此行为，因此Watchers的异步处理很复杂

        // thenAccept（）处理正常的观察者触发。
        watchedStage.thenAccept(watchedEvent -> {
            log.warn(watchedEvent.getType().toString());
            log.warn(watchedEvent.toString());
        });

        // 如果在这种情况下存在连接问题，则会异常调用观察者触发发出连接问题的信号。
        // 必须调用reset() 重置watched的stage
        watchedStage.exceptionally(exception -> {
            AsyncEventException asyncEx = (AsyncEventException) exception;
            log.error("AsyncEventException : {}",asyncEx);
            handleWatchedStage(asyncEx.reset());
            return null;
        });

    }
}
