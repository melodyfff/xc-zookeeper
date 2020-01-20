package com.xinchen.zookeeper.cloud.service;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringCloudZookeeperApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringCloudZookeeperApplication.class).web(WebApplicationType.SERVLET).run(args);
    }
}
