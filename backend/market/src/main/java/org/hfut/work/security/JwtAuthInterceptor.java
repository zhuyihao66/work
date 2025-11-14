package org.hfut.work.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.hfut.work.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Value("${app.jwt.secret}")
    private String secret;

    private Key key;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = md.digest(raw);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            this.key = Keys.hmacShaKeyFor(raw);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            return true; // public endpoints
        }
        // Public read-only marketplace endpoints
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            // Public item apis only: list, detail (numeric id), categories, nav
            if (path.equals("/api/items") ||
                path.equals("/api/items/categories") ||
                path.equals("/api/items/nav") ||
                path.matches("^/api/items/\\d+$") ||
                path.matches("^/api/items/seller/\\d+$")) {
                return true;
            }
            // Public user info (for store pages)
            if (path.matches("^/api/users/\\d+$")) {
                return true;
            }
            // Public review list for items
            if (path.matches("^/api/reviews/item/\\d+$")) {
                return true;
            }
            // File proxy for TOS images (public access)
            if (path.startsWith("/api/files/proxy")) {
                return true;
            }
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, "未授权：缺少或非法的Authorization头");
            return false;
        }
        String token = header.substring(7);
        try {
            Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            request.setAttribute("jwtClaims", jws.getBody());
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "未授权：无效或过期的令牌");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}


