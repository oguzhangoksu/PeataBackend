package peata.backend.service.concretes;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import peata.backend.entity.User;
import peata.backend.repositories.UserRepository;
import peata.backend.utils.UserPrincipal;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; 

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);


    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(identifier);
            if (user.isEmpty()) {
                logger.error("User not found with username or email: {}", identifier);
                throw new UsernameNotFoundException("User not found with username or email: " + identifier);
            }
            logger.info("User found by email or username: {}", identifier);
        }
        return UserPrincipal.create(user.get());
    }

   
}