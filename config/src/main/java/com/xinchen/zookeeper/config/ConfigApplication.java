package com.xinchen.zookeeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * zookeeper做为配置中心的例子
 *
 * 使用{@link Value}或者{@link ConfigurationProperties} 在配置了{@link RefreshScope}的前提下均可自动刷新
 *
 */
@SpringBootApplication
public class ConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }

}
