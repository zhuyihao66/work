package org.hfut.work.websocket;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.hfut.work.security.JwtUtil;
import org.hfut.work.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Objects;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private NotificationService notificationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = parseUserId(session);
        if (userId == null) {
            session.close(new CloseStatus(HttpStatus.UNAUTHORIZED.value(), "未授权"));
            return;
        }
        session.getAttributes().put("uid", userId);
        notificationService.register(userId, session);
        log.debug("WS connected: user {}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object uid = session.getAttributes().get("uid");
        if (uid instanceof Long) {
            notificationService.unregister((Long) uid, session);
        }
        log.debug("WS closed: {}", status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // keep-alive ping/pong, ignore payload
    }

    private Long parseUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        String token = params.getFirst("token");
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Claims claims = jwtUtil.parseClaims(token);
            Object uid = claims.get("uid");
            if (uid instanceof Number) {
                return ((Number) uid).longValue();
            }
        } catch (Exception e) {
            log.warn("Invalid WS token: {}", e.getMessage());
        }
        return null;
    }
}


