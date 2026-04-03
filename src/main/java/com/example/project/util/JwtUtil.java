package com.example.project.util;

import com.example.project.enums.Role;
import com.example.project.metrics.JwtMetricsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private final JwtMetricsService jwtMetrics;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, UUID userId, List<Role> roles) {
        List<String> authorities = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String token = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        jwtMetrics.incrementGenerated();
        return token;
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        Object auth = claims.get("authorities");
        if (auth instanceof List<?>) {
            return ((List<?>) auth).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            jwtMetrics.incrementValidated();
            return true;
        } catch (Exception e) {
            jwtMetrics.incrementInvalid();
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        String userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }
}