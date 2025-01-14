package peata.backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.logging.Logger;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
@Component
public class JwtProvider {

    @Value("${JWT_SECRET}")
    private String jwtSecret; 
    // private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Use a stronger key in production
    private final long jwtExpirationMs = 7776000000L; // 24 hours
    Logger logger = Logger.getLogger(this.getClass().getName());
    

    public String generateToken(Authentication authentication) {
        // System.out.println("KEY:" + Base64.getEncoder().encodeToString(jwtSecret.getEncoded()));
        System.out.println("jwtSecret:"+jwtSecret);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        System.out.println("User Principal: " + userPrincipal.toString());
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities()) // Include roles in claims
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
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