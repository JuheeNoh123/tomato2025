package com.sku_likelion.Moving_Cash_back.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.repository.SessionRepository;
import com.sku_likelion.Moving_Cash_back.security.JwtUtility;
import com.sku_likelion.Moving_Cash_back.service.CalculateService;
import com.sku_likelion.Moving_Cash_back.service.MovingService;
import com.sku_likelion.Moving_Cash_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LocationWebSocketHandler extends TextWebSocketHandler {
    private final JwtUtility jwtUtility;
    private final ObjectMapper objectMapper;
    private final MovingService movingService;
    private final UserService userService;
    private final SessionRepository sessionRepository;
    private final CalculateService calculateService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        String query = session.getUri().getQuery(); // token=JWT
        String token = query.substring("token=".length());
        if(token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        if(!jwtUtility.validateJwt(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        System.out.println("연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
        String query = socketSession.getUri().getQuery(); // token=JWT
        String token = query.substring("token=".length());
        if(token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        User user = userService.tokenToUser(token); // JWT에서 userId 추출

        Session session = sessionRepository.findByUser(user);

        if(session == null) {
            socketSession.sendMessage(new TextMessage("진행 중인 세션이 없습니다."));
            return;
        }

        // 클라이언트에서 받은 위치 데이터
        MovingReqDTO.RoutePointDTO dto = objectMapper.readValue(message.getPayload(), MovingReqDTO.RoutePointDTO.class);
        dto.setSessionId(session.getId());



        MovingResDTO.RunningStats stats = calculateService.updateStats(dto, session);
        dto.setDistance(stats.getTotalDistance());
        movingService.saveRoutePoint(dto);
        // 서버에서 클라이언트에게 응답
        socketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(stats)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("연결 종료: " + session.getId());
    }
}
