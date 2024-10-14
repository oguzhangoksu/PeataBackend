package peata.backend.service.concretes;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.listeners.DynamicListenerService;
import peata.backend.repositories.UserRepository;
import peata.backend.service.abstracts.UserService;


@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;

    
    @Autowired
    private NotificationServiceImpl notificationServiceImpl;

    @Autowired
    private DynamicListenerService dynamicListenerService;

    public User save(User user){
        User userDb=userRepository.save(user);

        
        notificationServiceImpl.subscribeUserToCityDistrict(user.getEmail(), user.getCity(), user.getDistrict());
        dynamicListenerService.createListener( user.getCity(), user.getDistrict());
        return userDb;
        
    }

    public void addFavorite(Long AddId,String username){
        User user=findUserByUsername(username);
        user.getFavoriteAdds().add(AddId);
        userRepository.save(user);
    }

    public void delete(Long id){
        User user =userRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("User with ID " + id + " not found"));
        userRepository.delete(user);;
    }
    public List<User> allUsers(){
        List<User> usersDb= userRepository.findAll();
        return usersDb;
    }

    public User findUserById(Long id){
        User user =userRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("User with ID " + id + " not found"));
        return user;
    }

    public Set<Add> findUsersAddsById(String username){
        User user =userRepository.findByUsername(username).orElseThrow(()-> new EntityNotFoundException("User with Username " + username + " not found"));
        return user.getAds();
    }
    
    public Page<User> getPaginatedUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public boolean changeNotificationStatus(User user){
        user.setIsAllowedNotification(!user.getIsAllowedNotification());
        userRepository.save(user);
        return user.getIsAllowedNotification();
    }

    public boolean isUsernameExist(String username){
        return userRepository.findByUsername(username).isPresent();
    }
    public boolean isEmailExist(String email){
        return userRepository.findByEmail(email).isPresent();
    }
    public List<String> findEmailsByCityAndDistrict(String city,String email,String publisherEmail){
        return userRepository.findEmailsByCityAndDistrict(city, email,publisherEmail);
    }
    public User findUserByUsername(String username){
        User user= userRepository.findByUsername(username)
            .orElseThrow(()-> new EntityNotFoundException("User with  " + username + " not found"));

        return user;
    }
    

    
}
