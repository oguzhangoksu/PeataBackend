package peata.backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.logging.Logger;
import java.util.Date;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
@Component
public class JwtProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret; 
    
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    
    Logger logger = Logger.getLogger(this.getClass().getName());

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        System.out.println("jwtSecret:" + jwtSecret);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        System.out.println("User Principal: " + userPrincipal.toString());
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities()) // Include roles in claims
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } 
         catch (JwtException e) {
            logger.severe("ERROR: Invalid JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.severe("ERROR :JWT token is null or empty: " + e.getMessage());
        }
        return false;
    }
    
}