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
 * {@link Stat}说明:
 * <pre>
 *     zxid，也就是事务id， 为了保证事务的顺序一致性，zookeeper 采用了递增的事务id号（zxid）来标识事务。
 *     zxid是一个64位的数字：
 *      高32位是epoch（ZAB协议通过epoch编号来 区分 Leader 周期变化的策略）用来标识 leader 关系是否 改变，每次一个 leader 被选出来，它都会有一个新的 epoch=（原来的epoch+1），标识当前属于那个leader的 统治时期。
 *      低32位用于递增计数
 *
 *
 *     每次修改ZooKeeper状态都会收到一个zxid形式的时间戳，也就是ZooKeeper事务ID。
 *     事务ID是ZooKeeper中所有修改总的次序。
 *     每个修改都有唯一的zxid，如果zxid1小于zxid2，那么zxid1在zxid2之前发生
 *
 *     czxid          - 引起这个znode创建的事务ID，创建节点的事务的zxid（ZooKeeper Transaction Id）
 *     ctime          - znode被创建的时间戳,表示从1970-01-01T00:00:00Z开始以毫秒为单位的znode创建时间
 *     mzxid          - znode最后更新的事务ID
 *     mtime          - znode最后修改的时间戳(从1970年开始)
 *     pZxid          - znode最后更新的子节点的事务ID
 *     cversion       - znode子节点修改次数
 *     dataversion    - znode的数据所做的更改次数（Stat中为version）
 *     aclVersion     - znode的ACL进行更改的次数（Stat中为aversion）
 *     ephemeralOwner - znode是临时节点(ephemeral)，这个是znode拥有者的session id。如果不是临时节点则是0 | 0x0
 *     dataLength     - znode数据字段的长度
 *     numChildren    - znode的子节点的数量
 * </pre>
 *
 * {@link ACL}说明：
 * <pre>
 *     ACL的格式    <schema>:<id>:<perm>
 *
 *    scheme  采用何种方式授权:
 *    <p>
 * 　　   world：默认方式，相当于全部都能访问
 * 　　   auth：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
 * 　　   digest：即用户名:密码这种方式认证，这也是业务系统中最常用的。用 username:password 字符串来产生一个MD5串，然后该串被用来作为ACL ID。认证是通过明文发送username:password 来进行的，当用在ACL时，表达式为username:base64 ，base64是password的SHA1摘要的编码。
 * 　　   ip：使用客户端的主机IP作为ACL ID 。这个ACL表达式的格式为addr/bits ，此时addr中的有效位与客户端addr中的有效位进行比对。
 *    </p>
 *
 *    id   给谁授予权限:
 *    <p>
 *        IP     - 通常是一个IP地址或者地址段，如：192.168.0.1 或 192.168.0.1/24
 *        Digest - 自定义，通常是“username:BASE64(SHA-1(username:password))” ，可通过 echo -n root:root | openssl dgst -binary -sha1 | openssl base64 简单生成
 *        World  - 只有一个ID "anyone"
 *        Super  - 与Digest一样
 *    </p>
 *
 *    permission   授予什么权限：
 *    <p>
 *        CREATE、READ、WRITE、DELETE、ADMIN 也就是 增、删、改、查、管理权限，这5种权限简写为crwda
 *
 *        这5种权限中，delete是指对子节点的删除权限，其它4种权限指对自身节点的操作权限
 *
 *         CREATE    c 可以创建子节点
 * 　　    DELETE    d 可以删除子节点（仅下一级节点）
 * 　　    READ      r 可以读取节点数据及显示子节点列表
 * 　　    WRITE     w 可以设置节点数据
 * 　　    ADMIN     a 可以设置节点访问控制列表权限
 *    </p>
 *
 *
 *
 *     setAcl /newznode world:anyone:crdwa  == create /newznode 'ok'
 *     setAcl /newznode ip:192.168.0.164:cdwra,ip:127.0.0.1:cdwra
 *
 *     addauth digest username:pwd    #增加授权用户,明文用户名和密码
 *     setAcl /newznode auth:username:cdwra
 *
 *     setAcl /newznode digest:username:pwd:cdwra
 * </pre>
 *
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
