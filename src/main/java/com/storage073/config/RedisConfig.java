package com.storage073.config;

import com.aliyun.oss.model.PartETag;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration("redisConfig")
public class RedisConfig<V> {

    @Autowired
    @Lazy
    private ObjectMapper objectMapper1;

    @Bean("objectMapper1")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将 PartETag 类与 PartETagMixin 绑定
        objectMapper.addMixIn(PartETag.class, PartETagMixin.class);

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用自定义的 ObjectMapper 来处理序列化和反序列化
//        ObjectMapper objectMapper1 = objectMapper();
        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper1);

        // 设置 key 的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置 value 的序列化方式，使用自定义的 ObjectMapper
        template.setValueSerializer(jsonSerializer);
        // 设置 hash 的 key 的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置 hash 的 value 的序列化方式，使用自定义的 ObjectMapper
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
