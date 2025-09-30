package com.example.instructions.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  

    @Bean
    public NewTopic inboundTopic() {
        return TopicBuilder.name("instructions.inbound")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic outboundTopic() {
        return TopicBuilder.name("instructions.outbound")
                .partitions(1)
                .replicas(1)
                .build();
    }

  
}
