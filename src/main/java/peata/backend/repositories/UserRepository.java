package peata.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import peata.backend.entity.User;


public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u.email FROM User u WHERE u.city = :city AND u.district = :district AND u.isAllowedNotification= true AND u.email <> :publisherEmail")
    List<String> findEmailsByCityAndDistrict(String city, String district,String publisherEmail);
} 
