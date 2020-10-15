package com.xinchen.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 *
 * Apache提供的client实现
 *
 * @author xinchen
 * @version 1.0
 * @date 13/10/2020 09:48
 */
public class ZookeeperApiClient implements ZookeeperApi {

    private final ZooKeeper zooKeeper;

    public ZookeeperApiClient(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void create(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Override
    public void create(String path, byte[] data,Stat stat) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,stat);
    }

    @Override
    public void create(String path, byte[] data, Stat stat, long ttl) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_WITH_TTL,stat,ttl);
    }

    @Override
    public void createSequence(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.create(path,data,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT_SEQUENTIAL);
    }

    @Override
    public void createEphemeral(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    @Override
    public void createEphemeralSequence(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }


    @Override
    public void delete(String path) throws KeeperException, InterruptedException {
        // 每个节点都有一个版本，删除时可指定删除的版本，类似乐观锁
        // 这里的version 为 dataVersion , 设置为-1表示忽略版本
        zooKeeper.delete(path,-1);
    }

    @Override
    public  List<String> children(String path) throws KeeperException, InterruptedException {
        // 默认不添加watch
      return zooKeeper.getChildren(path,false);
    }

    @Override
    public boolean exists(String path) throws KeeperException, InterruptedException {
        // 默认不添加watch
        return null!=zooKeeper.exists(path,false);
    }

    @Override
    public byte[] getData(String path) {
        byte[] data = null;
        try {
            data = zooKeeper.getData(path, false, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public Stat getStat(String path) {
        Stat stat = new Stat();
        try {
            zooKeeper.getData(path, false, stat);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return stat;
    }
}
