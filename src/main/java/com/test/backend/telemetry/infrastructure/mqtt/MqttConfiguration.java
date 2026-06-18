package com.test.backend.telemetry.infrastructure.mqtt;

import com.test.backend.shared.infrastructure.persistence.configuration.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLSocketFactory;

@Configuration
public class MqttConfiguration {

    private final MqttProperties mqttProperties;

    public MqttConfiguration(MqttProperties mqttProperties) {
        this.mqttProperties = mqttProperties;
    }

    @Bean
    public MqttClient mqttClient() throws Exception {
        String protocol = mqttProperties.getProtocol();
        if (protocol == null) protocol = "tcp";
        
        String serverUri;
        if (protocol.equalsIgnoreCase("mqtts") || protocol.equalsIgnoreCase("ssl")) {
            serverUri = "ssl://" + mqttProperties.getHost() + ":" + mqttProperties.getPort();
        } else {
            serverUri = "tcp://" + mqttProperties.getHost() + ":" + mqttProperties.getPort();
        }

        String clientId = "SafeLab_Backend_" + MqttClient.generateClientId();
        return new MqttClient(serverUri, clientId, new MemoryPersistence());
    }
    
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        String protocol = mqttProperties.getProtocol();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);

        if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
            options.setUserName(mqttProperties.getUsername());
        }
        if (mqttProperties.getPassword() != null && !mqttProperties.getPassword().isEmpty()) {
            options.setPassword(mqttProperties.getPassword().toCharArray());
        }

        if (protocol != null && (protocol.equalsIgnoreCase("mqtts") || protocol.equalsIgnoreCase("ssl"))) {
            options.setSocketFactory(SSLSocketFactory.getDefault());
        }
        return options;
    }
}
