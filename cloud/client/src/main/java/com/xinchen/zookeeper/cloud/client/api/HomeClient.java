package com.xinchen.zookeeper.cloud.client.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author xinchen
 * @version 1.0
 * @date 20/01/2020 16:18
 */
@FeignClient(value = "xc-zookeeper-services",fallbackFactory = HystrixClientFallbackFactory.class)
public interface HomeClient {
    @GetMapping("/greeting")
    String consumer();
}
