package com.xinchen.zookeeper.cloud.service.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xinchen
 * @version 1.0
 * @date 20/01/2020 14:35
 */
@RestController
@RefreshScope
public class HelloController {

    @Value("${server.port}")
    private String port;

    @Value("${hello.message}")
    private String message;

    @GetMapping("/greeting")
    public String hello(){
        return String.format("Hello World! from : %s <br> Config Center message: %s",port,message);
    }
}
