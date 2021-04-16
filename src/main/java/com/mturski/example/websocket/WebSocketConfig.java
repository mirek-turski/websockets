package com.mturski.example.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
        .addInterceptors(new HttpSessionHandshakeInterceptor())
        // // Custom HandshakeHandler assignes a unique Principal for the given session.
        // // That way ONLY that session will receive back messages from the server.
        // // The client will need to prepend its path with '/user'
        // .setHandshakeHandler(new DefaultHandshakeHandler() {
        //     @Override
        //     protected Principal determineUser(
		// 	ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        //         return new Principal() {

        //             @Override
        //             public String getName() {
        //                 return UUID.randomUUID().toString();
        //             }

        //         };
        //     }
        // })
        .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
}