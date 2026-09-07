// configurations/WebSocketConfig.java
package com.fivetuat.fsoftprojectserver.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fivetuat.fsoftprojectserver.filters.StompAuthChannelInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-game").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws-game").setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-notification").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws-notification").setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-community").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws-community").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}