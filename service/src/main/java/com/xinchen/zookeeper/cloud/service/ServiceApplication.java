package com.xinchen.zookeeper.cloud.service;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

/**
 * {@link EnableCaching} 主要是开启caffeine缓存,其实也可以不用加该注解,配置文件中配置即可
 */
@EnableCaching
@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceApplication.class).web(WebApplicationType.SERVLET).run(args);
    }
}
