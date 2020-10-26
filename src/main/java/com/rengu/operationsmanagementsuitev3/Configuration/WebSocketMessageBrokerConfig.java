package com.rengu.operationsmanagementsuitev3.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 17:44
 **/

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    //deviceInfo  设备信息
    //onlineDevice  设备是否在线
    //deployProgress    扫描进度
    //initializationState  初始化状态

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println(">>>>>>>>>>>服务器启动成功");
        config.enableSimpleBroker("/deviceInfo", "/onlineDevice", "/deployProgress","/initializationState");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("......添加这个Endpoint，这样在网页中就可以通过websocket连接上服务了~~~~~~");
        registry.addEndpoint("/OMS").setAllowedOrigins("*").withSockJS();
    }
}
