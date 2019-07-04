package com.xinchen.zookeeper.tutorial.framework;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 *
 * CRUD的例子
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/5 0:01
 */
public class CrudExamples {
    public static void create(CuratorFramework client,String path,byte[] payload) throws Exception {
        // this will create the given ZNode with the given data
        client.create().forPath(path, payload);
    }

    public static void createEphemeral(CuratorFramework client,String path,byte[] payload) throws Exception {
        // 临时节点的创建
        // this will create the given EPHEMERAL ZNode with the given data
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public static String createEphemeralSequential(CuratorFramework client,String path,byte[] payload) throws Exception {
        // 以Curator[保护模式]创建临时有序节点

        // this will create the given EPHEMERAL-SEQUENTIAL ZNode with the given data using Curator protection.


        // 关于保护模式：
        /*
            Protection Mode:
            It turns out there is an edge case that exists when creating sequential-ephemeral nodes. The creation
            can succeed on the server, but the server can crash before the created node name is returned to the
            client. However, the ZK session is still valid so the ephemeral node is not deleted. Thus, there is no
            way for the client to determine what node was created for them.
            Even without sequential-ephemeral, however, the create can succeed on the sever but the client (for various
            reasons) will not know it. Putting the create builder into protection mode works around this. The name of
            the node that is created is prefixed with a GUID. If node creation fails the normal retry mechanism will
            occur. On the retry, the parent path is first searched for a node that has the GUID in it. If that node is
            found, it is assumed to be the lost node that was successfully created on the first try and is returned to
            the caller.
         */
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public static void setData(CuratorFramework client,String path,byte[] payload) throws Exception {
        // set data for the given node
        // 如果该path不存在则会抛出KeeperException.NoNodeException异常
        client.setData().forPath(path, payload);
    }

    public static void setDataAsync(CuratorFramework client,String path,byte[] payload) throws Exception {
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                //检查事件以获取详细信息
                switch (curatorEvent.getType()){
                    case CREATE:
                        System.out.println("CREATE");
                        break;
                    case DELETE:
                        System.out.println("DELETE");
                        break;
                    default:
                        break;
                }
            }
        };

        //异步设置给定节点的数据。 完成通知CuratorListener完成。
        client.getCuratorListenable().addListener(listener);
        client.setData().inBackground().forPath(path, payload);
    }

    public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback,
                                                String path,byte[] payload) throws Exception {

        // 这是另一种获取异步完成通知的方法
        client.setData().inBackground(callback).forPath(path, payload);
    }

    public static void delete(CuratorFramework client,String path) throws Exception {

        // 删除节点
        client.delete().forPath(path);
    }

    public static void guaranteedDelete(CuratorFramework client,String path) throws Exception {
        // 删除给定节点并保证它完成
        // delete the given node and guarantee that it completes

        // 关于担保删除:

        /*
            Guaranteed Delete
            Solves this edge case: deleting a node can fail due to connection issues. Further, if the node was
            ephemeral, the node will not get auto-deleted as the session is still valid. This can wreak havoc
            with lock implementations.
            When guaranteed is set, Curator will record failed node deletions and attempt to delete them in the
            background until successful. NOTE: you will still get an exception when the deletion fails. But, you
            can be assured that as long as the CuratorFramework instance is open attempts will be made to delete
            the node.
         */

        // 如果设置了保证，Curator将记录失败的节点删除并尝试删除它们直到成功。
        // 注意：删除失败时仍会出现异常。 但是只要CuratorFramework实例打开，就会尝试删除节点。

        client.delete().guaranteed().forPath(path);
    }


    public static List<String> watchedGetChildren(CuratorFramework client,String path) throws Exception {
         // 获取孩子并在节点上设置观察者。
         // 观察者通知将通过CuratorListener(参见上面的setDataAsync())。

        return client.getChildren().watched().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
        // 获取孩子并在节点上设置观察者。
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }
}
