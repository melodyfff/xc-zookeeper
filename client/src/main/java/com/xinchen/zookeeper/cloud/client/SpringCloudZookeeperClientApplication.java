package com.xinchen.zookeeper.cloud.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableHystrix
@EnableFeignClients
@SpringBootApplication
public class SpringCloudZookeeperClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudZookeeperClientApplication.class, args);
    }

}
