package com.sku_likelion.Moving_Cash_back.config;

import com.sku_likelion.Moving_Cash_back.handler.LocationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@RequiredArgsConstructor
@Configuration
@EnableWebSocket //웹 소켓 서버를 활성화 할때 사용
public class WebSocketConfig implements WebSocketConfigurer { //프론트에서 구독할 경로 및 적용시킬 브로커, 인터셉터 등 설정
    private final LocationWebSocketHandler locationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { //결과적으로 응답이 websocket:true면 웹소켓 연결됨
        registry.addHandler(locationWebSocketHandler, "/ws/location")
                .setAllowedOrigins("*"); // CORS 허용
    }
}
