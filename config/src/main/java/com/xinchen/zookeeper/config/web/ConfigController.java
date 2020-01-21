package com.xinchen.zookeeper.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xinchen
 * @version 1.0
 * @date 21/01/2020 11:26
 */
@RestController
@RefreshScope
public class ConfigController {
    @Value("${hello.message}")
    private String message;

    @GetMapping
    public String message(){
        return message;
    }
}
