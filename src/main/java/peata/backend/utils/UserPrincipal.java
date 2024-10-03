package peata.backend.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import peata.backend.entity.User;
import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private User user; // Assuming you have a User entity

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // Assuming your User entity has a 'username' field
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Assuming your User entity has a 'password' field
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> user.getRole());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Custom logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Custom logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Custom logic if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Custom logic if needed
    }
    @Override
    public String toString(){
        return "Username:"+ getUsername()+" Password:" + getPassword() +".";
    }
}
