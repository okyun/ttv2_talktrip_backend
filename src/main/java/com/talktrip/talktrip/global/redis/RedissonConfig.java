package com.talktrip.talktrip.global.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 재고 등 분산 락용 Redisson 클라이언트.
 * Spring Data Redis의 {@code spring.data.redis.host/port}와 동일한 인스턴스를 사용합니다.
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password
    ) {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        var single = config.useSingleServer().setAddress(address);
        if (StringUtils.hasText(password)) {
            single.setPassword(password);
        }
        return Redisson.create(config);
    }
}
