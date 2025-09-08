package com.study.odersystem.common.config;

import com.study.odersystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // red pub/sub기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }
    
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> ssePubSubTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis pub/sub 리스너 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("ordering"));
        return container;
    }

    // redis의 채널에서 수신된 메세지를 처리하는 빈 객체
    @Bean
    public MessageListenerAdapter listenerAdapter(SseAlarmService sseAlarmService) {
        // 채널로 부터 수신되는 message 처리를 SseAlarmService 객체로 던져주고, SseAlarmService의 onMessage 메서드에서 처리한다.
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
