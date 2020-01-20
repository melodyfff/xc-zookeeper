package com.xinchen.zookeeper.cloud.service.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xinchen
 * @version 1.0
 * @date 20/01/2020 14:35
 */
@RestController
public class HelloController {
    @GetMapping("/greeting")
    public String hello(){
        return "Hello World!";
    }
}
