package com.shsh.auth_service_social_network.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.minutes}")
    private int jwtExpirationMinutes;

    public String generateJwtToken( String email, String id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMinutes * 60000L);

        System.out.println("Token generated at: " + now);
        System.out.println("Token expires at: " + expiryDate);

        return Jwts.builder()
                .setSubject(email)
                .setId(id)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }
    // Метод для генерации refresh token
    public String generateRefreshToken( String email, String id) {
        // Например, срок действия refresh token в 7 дней
        int refreshTokenExpirationMinutes = 7 * 24 * 60;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMinutes * 60000L);

        return Jwts.builder()
                .setSubject(email)
                .setId(id)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            System.out.println("error jwt validate");
        }
        return false;
    }
}