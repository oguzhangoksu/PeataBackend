package peata.backend.service.concretes;

import java.util.Optional;

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

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // First, try to load by username
        Optional<User> user = userRepository.findByUsername(identifier);
        if (user.isEmpty()) {
            // If not found by username, try to load by email
            user = userRepository.findByEmail(identifier);
            if (user.isEmpty()) {
                throw new UsernameNotFoundException("User not found with username or email: " + identifier);
            }
        }
        return UserPrincipal.create(user.get());
    }

    /* @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return new UserPrincipal(user); // Convert User to UserPrincipal
    } */
}