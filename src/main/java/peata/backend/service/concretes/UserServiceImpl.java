package peata.backend.service.concretes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.PasswordResetCode;
import peata.backend.entity.User;
import peata.backend.listeners.DynamicListenerService;
import peata.backend.repositories.PasswordResetCodeRepository;
import peata.backend.repositories.UserRepository;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.GenerateCode;


@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Lazy
    @Autowired
    private AddService addService;
    
    @Autowired
    private NotificationServiceImpl notificationServiceImpl;

    @Autowired
    private DynamicListenerService dynamicListenerService;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private GenerateCode generateCode;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user){
        User userDb=userRepository.save(user);

        
        notificationServiceImpl.subscribeUserToCityDistrict(user.getEmail(), user.getCity(), user.getDistrict());
        dynamicListenerService.createListener( user.getCity(), user.getDistrict());
        return userDb;
        
    }

    public boolean addFavorite(Long AddId,String username){
        User user=findUserByUsername(username);

        if(user.getFavoriteAdds()==null){
            user.setFavoriteAdds(new ArrayList<>());
        }
        AddDto add = addService.findAddById(AddId);
        if (add == null) {
            return false;
        }
        if (user.getFavoriteAdds().contains(AddId)) {
            return true; // Ad is already in favorites, no need to add again
        }
        user.getFavoriteAdds().add(AddId);
        userRepository.save(user);
        return true;
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

   public String createPaswwordResetCode(String identifier) {
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        System.out.println(userOpt.isPresent() ? userOpt.get() : "User not found by username");
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(identifier);
            System.out.println(userOpt.isPresent() ? userOpt.get() : "User not found by email");
            if (!userOpt.isPresent()) {
                return "There is no such person";
            }
        }

        User userDb = userOpt.get(); 

        PasswordResetCode passwordResetCode = new PasswordResetCode();
        passwordResetCode.setEmail(userDb.getEmail());
        passwordResetCode.setExpirationTime(LocalDateTime.now().plus(5, ChronoUnit.MINUTES));
        passwordResetCode.setCode(generateCode.generateVerificationCode());

        passwordResetCodeRepository.save(passwordResetCode);

        try{
            emailServiceImpl.sendVerificationCode(passwordResetCode.getEmail(), passwordResetCode.getCode());
        }
        catch(MessagingException e){
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }
        

        return "Password reset token generated and sent";
    }
        


    public boolean validateVerificationCode(String email, String code) {
        List<PasswordResetCode>listPasswordResetCode= passwordResetCodeRepository.findByEmail(email);
    
        if (listPasswordResetCode.isEmpty()) {
            return false; // No tokens found for the given email
        }
        PasswordResetCode lastOne=listPasswordResetCode.get(listPasswordResetCode.size()-1);
        System.out.println(lastOne.getCode());
        System.out.println(lastOne.getEmail());
        if (lastOne.getCode().equals(code) && lastOne.getEmail().equals(email)) {
            return  lastOne.getExpirationTime().isAfter(LocalDateTime.now());
        }
        return false;
    }
    //lastOne.getExpirationTime().isAfter(LocalDateTime.now())
    @Transactional
    public void updatePassword(String email, String newPassword) {
        Optional<User> userDb=userRepository.findByEmail(email);
        if(userDb.isPresent()){
            userDb.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(userDb.get());   
            passwordResetCodeRepository.deleteByEmail(email);
        }
        else{
            System.out.println("There is no such person : email");
        }
        
       
    }

    public boolean deleteFavorite(User user,Long AddId){
        boolean adExists = user.getFavoriteAdds().stream()
                             .anyMatch(add -> add == AddId);
        if(adExists){
            user.getFavoriteAdds().removeIf(ad -> ad == AddId);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    
}


