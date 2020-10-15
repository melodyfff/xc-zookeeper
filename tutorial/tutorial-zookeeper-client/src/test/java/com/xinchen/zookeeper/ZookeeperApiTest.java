package com.xinchen.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 *
 * zk上测试数据：  /hello/ok
 *
 * @author xinchen
 * @version 1.0
 * @date 13/10/2020 14:41
 */
public class ZookeeperApiTest {

    private final ZookeeperApi client;
    private static final String ROOT_PATH = "/hello";
    private static final String NOT_EXIST_PATH = "/THIS_IS_A_TEST_PATH_NOT_EXIST";
    private static final String MOCK_STRING = "OK";
    private static final byte[] MOCK_DATA = MOCK_STRING.getBytes();

    public ZookeeperApiTest() throws IOException {
        client = new ZookeeperApiClient(new ZooKeeper("127.0.0.1:2181", 3000, null));
    }


    @Test
    public void create() throws KeeperException, InterruptedException {
        if (!client.exists(ROOT_PATH)){
            client.create(ROOT_PATH, MOCK_DATA);
            client.create(ROOT_PATH+"/ok", MOCK_DATA);
        }
    }

    @Test
    public void createSequence() throws KeeperException, InterruptedException {
        client.createSequence(ROOT_PATH, MOCK_DATA);
    }

    @Test
    public void createEphemeral() throws KeeperException, InterruptedException {
        client.createEphemeral(ROOT_PATH+"/tmp111", MOCK_DATA);
    }

    @Test
    public void delete() {
    }

    @Test
    public void children() throws KeeperException, InterruptedException {
        List<String> children = client.children(ROOT_PATH);
        assertTrue(children.size()>0);

    }

    @Test
    public void childrenRecursion() throws KeeperException, InterruptedException {
        TreeNode treeNode = client.childrenRecursion(ROOT_PATH);
        assertNotNull(treeNode);
    }

    @Test
    public void deleteIgnoreError() {
        client.deleteIgnoreError(NOT_EXIST_PATH);
    }

    @Test
    public void exists() throws KeeperException, InterruptedException {
        assertFalse(client.exists(NOT_EXIST_PATH));
    }

    @Test
    public void getData() {
        assertEquals(MOCK_STRING,new String(client.getData(ROOT_PATH), StandardCharsets.UTF_8));
    }

    @Test
    public void getStat() {
        client.getStat(ROOT_PATH);
    }

}