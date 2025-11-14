package org.hfut.work.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expireMinutes:120}")
    private long expireMinutes;

    private Key key;

    @PostConstruct
    public void init() {
        // Accept raw or base64 secret; derive 256-bit key via SHA-256 to avoid WeakKeyException
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException | DecodingException ex) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = md.digest(raw);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            // Fallback to direct HMAC key (will still fail if too short)
            this.key = Keys.hmacShaKeyFor(raw);
        }
    }

    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("role", role);
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiry = Date.from(now.plusSeconds(expireMinutes * 60));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return jws.getBody();
    }
}


