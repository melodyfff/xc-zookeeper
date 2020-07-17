package com.xinchen.zookeeper.cloud.client.api;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author xinchen
 * @version 1.0
 * @date 20/01/2020 16:29
 */
@Component
public class HystrixClientFallbackFactory implements FallbackFactory {
    @Override
    public HomeClient create(Throwable throwable) {
        return ()-> "feign + hystrix ,提供者服务挂了";
    }
}
