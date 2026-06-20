package com.test.backend.shared.infrastructure.persistence.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mqtt")
@Getter
@Setter
public class MqttProperties {
    private String host;
    private int port;
    private String protocol;
    private String username;
    private String password;
    private String topic;
}
