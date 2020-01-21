package com.xinchen.zookeeper.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring cloud gateway : https://stackoverflow.com/questions/47092048/how-is-spring-cloud-gateway-different-from-zuul
 * 官网文档: https://cloud.spring.io/spring-cloud-gateway/reference/html/
 *
 * RoutePredicateFactory
 *
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
