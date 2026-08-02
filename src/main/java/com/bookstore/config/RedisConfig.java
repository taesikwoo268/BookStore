package com.bookstore.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Cấu hình Cache Manager với TTL khác nhau cho từng cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình default
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("bookstore:");

        // Cấu hình TTL cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // ✅ Book detail: TTL 10 phút
        cacheConfigurations.put("bookDetail", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // ✅ Category list: TTL 5 phút
        cacheConfigurations.put("categoryList", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // ✅ Homepage featured: TTL 1 phút
        cacheConfigurations.put("homepageFeatured", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // ✅ Book list (optional): TTL 3 phút
        cacheConfigurations.put("bookList", defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // ✅ Cart (optional): TTL 30 phút
        cacheConfigurations.put("cart", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put("topSellers", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}