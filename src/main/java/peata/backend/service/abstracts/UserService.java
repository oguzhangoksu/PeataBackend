package peata.backend.service.abstracts;

import java.util.List;

import org.springframework.data.domain.Page;

import peata.backend.entity.Add;
import peata.backend.entity.User;

import java.util.Set;

public interface UserService {
    public User save(User user);
    public void addFavorite(Long AddId,String username);
    public void delete(Long id);
    public List<User> allUsers();
    public User findUserById(Long id);
    public Set<Add> findUsersAddsById(Long id);
    public Page<User> getPaginatedUsers(int page, int size);
    public boolean changeNotificationStatus(User user);
    public boolean isUsernameExist(String username);
    public boolean isEmailExist(String email);
    public List<String>findEmailsByCityAndDistrict(String city, String email, String publisherEmail);
    public User findUserByUsername(String username);
} 
