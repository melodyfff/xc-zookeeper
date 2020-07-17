package com.xinchen.zookeeper.tutorial.framework;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;

import java.util.Collection;

/**
 * 事物的例子
 *
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/7/5 0:36
 */
@Slf4j
public class TransactionExamples {

    public static Collection<CuratorTransactionResult> transaction(CuratorFramework client) throws Exception {
        // this example shows how to use ZooKeeper's transactions
        CuratorOp createOp = client.transactionOp().create().forPath("/a/path", "some data".getBytes());
        CuratorOp setDataOp = client.transactionOp().setData().forPath("/another/path", "other data".getBytes());
        CuratorOp deleteOp = client.transactionOp().delete().forPath("/yet/another/path");


        Collection<CuratorTransactionResult> results = client.transaction().forOperations(createOp, setDataOp, deleteOp);

        results.forEach((result -> log.info("{}-{}",result.getForPath(),result.getType())));

        return results;
    }
}
