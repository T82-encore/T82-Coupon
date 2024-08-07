package com.T82.coupon.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;

@Configuration
public class KafkaConfig {

    @Bean
    public RetryTopicConfiguration retryableTopic(KafkaTemplate<String, Object> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .maxAttempts(3)
                .exponentialBackoff(10 * 1000L, 2, 5 * 60 * 1000L)
                .autoCreateTopics(true, 3, (short) 3)
                .retryOn(IllegalArgumentException.class)
                .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
                .dltProcessingFailureStrategy(DltStrategy.ALWAYS_RETRY_ON_ERROR)
                .create(template);
    }
}