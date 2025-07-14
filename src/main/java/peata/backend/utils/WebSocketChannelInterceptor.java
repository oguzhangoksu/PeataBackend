package peata.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

import peata.backend.service.concretes.CustomUserDetailsService;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            // CONNECT, SUBSCRIBE ve SEND komutlarını yakalayalım
            if (StompCommand.CONNECT.equals(accessor.getCommand()) || 
                StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || 
                StompCommand.SEND.equals(accessor.getCommand())) {
                
                // Authorization header'ından token'ı al
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String jwt = authHeader.substring(7);
                        
                        if (jwtProvider.validateJwtToken(jwt)) {
                            String username = jwtProvider.getUsernameFromJwtToken(jwt);
                            System.out.println("Valid JWT token found for user: " + username);
                            System.out.println("DEBUG: username variable value: '" + username + "'");
                            
                            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                            System.out.println("DEBUG: userDetails loaded: " + userDetails.getUsername());
                            
                            // UserPrincipal oluştur - constructor: (id, username, email, password, authorities)
                            UserPrincipal userPrincipal = new UserPrincipal(null, username, null, null, userDetails.getAuthorities());
                            System.out.println("DEBUG: UserPrincipal created with username: '" + userPrincipal.getUsername() + "'");
                            System.out.println("DEBUG: UserPrincipal toString: " + userPrincipal.toString());
                            
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userPrincipal, null, userDetails.getAuthorities());
                            
                            System.out.println("DEBUG: Authentication principal: " + authentication.getPrincipal().toString());
                            
                            // Authentication'ı hem accessor'a hem de SecurityContext'e set et
                            accessor.setUser(authentication);
                            
                            // SecurityContextHolder'a da ekle (ChatController için gerekli)
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            System.out.println("WebSocket authentication successful for user: " + username);
                        } else {
                            System.out.println("Invalid JWT token in WebSocket");
                        }
                    } catch (Exception e) {
                        System.out.println("JWT validation failed: " + e.getMessage());
                        throw new RuntimeException("Invalid JWT token", e);
                    }
                } else {
                    System.out.println("No Authorization header found in WebSocket message");
                }
            }
        }
        
        return message;
    }
}
