package com.xinchen.zookeeper.embedded;

import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author xin chen
 * @version 1.0.0
 * @date 2021/1/13 13:55
 */
public class EmbeddedZooKeeperTest {

    @Test
    public void start() throws InterruptedException {
        new EmbeddedZooKeeper(2181, false).start();
        new CountDownLatch(1).await();
    }

    /**
     * spring管理生命周期
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void springContext() throws InterruptedException {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);

        // 获取beanFactory动态注入
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) (context).getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EmbeddedZooKeeperSpring.class);
        // beanDefinitionBuilder.addPropertyReference()
        beanFactory.registerBeanDefinition("em-zookeeper",beanDefinitionBuilder.getRawBeanDefinition());

        EmbeddedZooKeeperSpring embeddedZooKeeper = context.getBean(EmbeddedZooKeeperSpring.class);
        embeddedZooKeeper.start();

        TimeUnit.SECONDS.sleep(5);

        context.stop(); // main  WARN embedded.EmbeddedZooKeeper: context try to stop embedded zookeeper...
    }

}