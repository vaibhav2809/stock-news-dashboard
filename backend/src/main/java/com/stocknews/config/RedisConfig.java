package com.stocknews.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Map;

/**
 * Redis cache configuration with per-cache TTLs.
 * Uses a custom ObjectMapper with JSR-310 (Java 8 date/time) support
 * to properly serialize OffsetDateTime and other java.time types.
 * Disabled in "test" profile to avoid requiring a Redis connection during unit tests.
 */
@Configuration
@EnableCaching
@Profile("!test")
public class RedisConfig {

    /**
     * Creates a Redis-specific ObjectMapper with JSR-310 module and type info.
     * This is separate from Spring Boot's HTTP ObjectMapper because Redis needs
     * polymorphic type info to correctly deserialize cached values.
     * @return configured ObjectMapper for Redis serialization
     */
    private ObjectMapper redisObjectMapper() {
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL)
                .build();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper())))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "news", defaultConfig.entryTtl(Duration.ofMinutes(15)),
                "quotes", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "sentiment", defaultConfig.entryTtl(Duration.ofMinutes(30)),
                "trending", defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
