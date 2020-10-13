package com.xinchen.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 *
 * zookeeper api
 *
 * @author xinchen
 * @version 1.0
 * @date 13/10/2020 09:46
 */
public interface ZookeeperApi {
    void create(String path, byte data[], List<ACL> acl, CreateMode createMode) throws KeeperException, InterruptedException;

    void delete(String path) throws KeeperException, InterruptedException;

    /**
     * return children list that current node include.
     *
     * there is no watch add on.
     *
     * @param path path
     * @return children list
     * @throws KeeperException KeeperException
     * @throws InterruptedException InterruptedException
     */
    List<String> children(String path) throws KeeperException, InterruptedException;

    /**
     * is node path exists?
     *
     * there is no watch add on.
     *
     * @param path path
     * @return true/false
     * @throws KeeperException KeeperException
     * @throws InterruptedException InterruptedException
     */
    boolean exists(String path) throws KeeperException, InterruptedException;

    /**
     * get data
     * @param path path
     * @return byte[] or null
     */
    byte[] getData(String path);

    Stat getStat(String path);

    /**
     * recursion get children ,wrap by {@link TreeNode}
     *
     * @param path root path
     * @return TreeNode
     * @throws KeeperException KeeperException
     * @throws InterruptedException InterruptedException
     */
    default TreeNode childrenRecursion(String path) throws KeeperException, InterruptedException {
        TreeNode root = new TreeNode(path);
        List<TreeNode> childrenNode = root.getChildren();

        for (String childrenPath : children(path)) {
            // recursion
            childrenNode.add(childrenRecursion(root.getPath()+"/"+childrenPath));
        }

        return root;
    }


    /**
     * delete path node , ignore error
     * @param path node path
     */
    default void deleteIgnoreError(String path){
        try {
            delete(path);
        } catch (KeeperException | InterruptedException ignore) {
            // ignore
        }
    }
}
