package com.xinchen.zookeeper.cloud.client.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xinchen
 * @version 1.0
 * @date 20/01/2020 16:31
 */
@RestController
public class ClientController {
    private final HomeClient homeClient;

    public ClientController(HomeClient homeClient) {
        this.homeClient = homeClient;
    }

    @GetMapping("/")
    public String hello(){
        return homeClient.consumer();
    }
}
