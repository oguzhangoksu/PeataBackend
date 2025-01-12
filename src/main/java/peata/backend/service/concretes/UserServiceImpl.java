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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import peata.backend.dtos.AddDto;
import peata.backend.dtos.UserDto;
import peata.backend.entity.Add;
import peata.backend.entity.PasswordResetCode;
import peata.backend.entity.RegisterCode;
import peata.backend.entity.User;
import peata.backend.entity.UserDevice;
import peata.backend.listeners.DynamicListenerService;
import peata.backend.repositories.PasswordResetCodeRepository;
import peata.backend.repositories.RegisterCodeRepository;
import peata.backend.repositories.UserDeviceRepository;
import peata.backend.repositories.UserRepository;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.GenerateCode;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.UserUpdateRequest;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
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
    private RegisterCodeRepository registerCodeRepository;

    @Autowired
    private GenerateCode generateCode;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDeviceRepository userDeviceRepository;


    public User save(User user) {
        logger.info("Saving user with email: {}", user.getEmail());
        User userDb = userRepository.save(user);
        notificationServiceImpl.subscribeUserToCityDistrict(user.getEmail(), user.getCity(), user.getDistrict());
        dynamicListenerService.createListener(user.getCity(), user.getDistrict());
    
        RegisterCode registerCode = new RegisterCode();
        registerCode.setEmail(userDb.getEmail());
        registerCode.setExpirationTime(LocalDateTime.now().plus(5, ChronoUnit.MINUTES));
        registerCode.setCode(generateCode.generateVerificationCode());
        registerCodeRepository.save(registerCode);
        notificationServiceImpl.sendRegisterCode(registerCode.getEmail(),registerCode.getCode());

        logger.info("Email verification message sent to RabbitMQ for email: {}", registerCode.getEmail());
        logger.info("User saved successfully with ID: {}", userDb.getId());
        return userDb;
    }
    public Boolean updateUser(String currentUsername,UserUpdateRequest user) {
        logger.info("Starting update process for user with email: {}", user.getEmail());
        Optional<User> optionalUserDb = userRepository.findByUsername(currentUsername);
        if (optionalUserDb.isEmpty()) {
            logger.warn("User with username {} not found. Update process aborted.", user.getUsername());
            return false;
        }
        logger.info("User with username {} found. Proceeding with update.", user.getUsername());
        
        User userDb = optionalUserDb.get();
        
        if(userDb.getCity() != user.getCity() || userDb.getDistrict() != user.getDistrict()){
            logger.info("City or district has changed for user ID {}. Updating subscription and listener.", userDb.getId());
            notificationServiceImpl.subscribeUserToCityDistrict(user.getEmail(), user.getCity(), user.getDistrict());
            dynamicListenerService.createListener(user.getCity(), user.getDistrict());
        }
        logger.debug("Updating details for user ID {}: email, password, name, surname, phone, city, and district.", userDb.getId());
        userDb.setUsername(user.getUsername());
        userDb.setEmail(user.getEmail());
        userDb.setName(user.getName());
        userDb.setSurname(user.getSurname());
        userDb.setPhone(user.getPhone());
        userDb.setCity(user.getCity());
        userDb.setDistrict(user.getDistrict());
        userDb.setIsAllowedNotification(user.getIsAllowedNotification());
        if(userDb.getEmailValidation()){
            userDb.setEmailValidation(true);
        }
        logger.info("User with ID {} successfully updated in the database.", userDb.getId());
        userRepository.save(userDb);
        logger.info("Update process for user with ID {} completed successfully.", userDb.getId());
        return true;
    }

    public boolean addFavorite(Long AddId,String username){
        logger.info("Adding ad with ID {} to user {}'s favorites", AddId, username);
        User user=findUserByUsername(username);

        if(user.getFavoriteAdds()==null){
            user.setFavoriteAdds(new ArrayList<>());
        }
        AddDto add = addService.findAddById(AddId);

        if (add == null) {
            logger.warn("Ad with ID {} not found", AddId);
            return false;
        }
        if (user.getFavoriteAdds().contains(AddId)) {
            logger.info("Ad with ID {} is already in user {}'s favorites", AddId, username);
            return true; // Ad is already in favorites, no need to add again
        }
        user.getFavoriteAdds().add(AddId);
        userRepository.save(user);
        logger.info("Ad with ID {} added to user {}'s favorites", AddId, username);
        return true;
    }

    public void delete(Long id){
        logger.info("Deleting user with ID {}", id);
        User user =userRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("User with ID " + id + " not found"));
        userRepository.delete(user);
        logger.info("User with ID {} deleted successfully", id);
    }
    public List<User> allUsers(){
        logger.info("Fetching all users");
        List<User> usersDb= userRepository.findAll();
        logger.info("Fetched {} users", usersDb.size());
        return usersDb;
    }

    public User findUserById(Long id){
        logger.info("Finding user by ID {}", id);
        User user =userRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("User with ID " + id + " not found"));
        return user;
    }

    public Set<Add> findUsersAddsById(String username){
        logger.info("Finding ads for user {}", username);
        User user =userRepository.findByUsername(username)
            .orElseThrow(()-> new EntityNotFoundException("User with Username " + username + " not found"));
        return user.getAds();
    }
    public Long findUserIdByUsername(String username) {
        logger.info("Finding user ID for username {}", username);
        return userRepository.findUserIdByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with Username " + username + " not found"));
    }
    
    public Page<User> getPaginatedUsers(int page, int size) {
        logger.info("Fetching paginated users: page {}, size {}", page, size);
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public boolean changeNotificationStatus(User user){
        logger.info("Changing notification status for user {}", user.getEmail());
        user.setIsAllowedNotification(!user.getIsAllowedNotification());
        userRepository.save(user);
        logger.info("Notification status for user {} changed to {}", user.getEmail(), user.getIsAllowedNotification());
        return user.getIsAllowedNotification();
    }

    public boolean isUsernameExist(String username){
        logger.info("Checking if username {} exists", username);
        return userRepository.findByUsername(username).isPresent();
    }
    public boolean isEmailExist(String email){
        logger.info("Checking if email {} exists", email);
        return userRepository.findByEmail(email).isPresent();
    }

    public List<String> findEmailsByCityAndDistrict(String city,String email,String publisherEmail){
        logger.info("Finding emails for city: {}, district: {}, excluding publisher: {}", city, email, publisherEmail);
        return userRepository.findEmailsByCityAndDistrict(city, email,publisherEmail);
    }

    public List<String> findEmailsByCityAndDistrictOnValidateEmail(String city,String email,String publisherEmail){
        logger.info("Finding emails for city: {}, district: {}, excluding publisher: {}", city, email, publisherEmail);
        return userRepository.findEmailsByCityAndDistrictOnValidateEmail(city, email,publisherEmail);
    }
    public User findUserByUsername(String username){
        logger.info("Finding user by username: {}", username);
        User user= userRepository.findByUsername(username)
            .orElseThrow(()-> new EntityNotFoundException("User with  " + username + " not found"));

        return user;
    }

   public String createPaswwordResetCode(String identifier) {
        logger.info("Creating password reset code for identifier: {}", identifier);
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        System.out.println(userOpt.isPresent() ? userOpt.get() : "User not found by username");
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(identifier);
            if (!userOpt.isPresent()) {
                logger.warn("No user found with identifier: {}", identifier);
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
            notificationServiceImpl.sendCodeVerification(passwordResetCode.getEmail(),passwordResetCode.getCode());
            logger.info("Password reset code sent to {}", passwordResetCode.getEmail());
        }
        catch(Exception e){
            logger.error("Failed to send verification email", e);
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }
        

        return "Password reset token generated and sent";
    }
        


    public boolean validateVerificationCode(String email, String code) {
        logger.info("Validating verification code for email: {}", email);
        List<PasswordResetCode>listPasswordResetCode= passwordResetCodeRepository.findByEmail(email);
        if (listPasswordResetCode.isEmpty()) {
            logger.warn("No verification codes found for email: {}", email);
            return false; // No tokens found for the given email
        }
        PasswordResetCode lastOne=listPasswordResetCode.get(listPasswordResetCode.size()-1);

        if (lastOne.getCode().equals(code) && lastOne.getEmail().equals(email)) {
            logger.info("Verification code validated for email: {}", email);
            return  lastOne.getExpirationTime().isAfter(LocalDateTime.now());
        }
        logger.warn("Invalid verification code for email: {}", email);
        return false;
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        logger.info("Updating password for email: {}", email);
        Optional<User> userDb=userRepository.findByEmail(email);
        if(userDb.isPresent()){
            userDb.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(userDb.get());   
            passwordResetCodeRepository.deleteByEmail(email);
            logger.info("Password updated for email: {}", email);
        }
        else{
            logger.warn("No user found for email: {}", email);
        }
        
       
    }
    
    public boolean deleteFavorite(User user,Long AddId){
        logger.info("Deleting favorite ad with ID {} for user {}", AddId, user.getEmail());
        boolean adExists = user.getFavoriteAdds().stream()
                             .anyMatch(add -> add == AddId);
        if(adExists){
            user.getFavoriteAdds().removeIf(ad -> ad == AddId);
            userRepository.save(user);
            logger.info("Favorite ad with ID {} removed for user {}", AddId, user.getEmail());
            return true;
        }
        logger.warn("Ad with ID {} not found in user {}'s favorites", AddId, user.getEmail());
        return false;
    }

    public boolean emailValidation(String email, String code) {
        logger.info("Starting email validation for email: {}", email);
        if (!validateRegisterCode(email, code)) {
            logger.warn("Validation failed for email: {} with code: {}", email, code);
            return false;
        }
        logger.info("Register code validation succeeded for email: {}", email);
    
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User foundUser = user.get();
            logger.info("User found for email: {}. Updating email validation status...", email);
            foundUser.setEmailValidation(true);
            userRepository.save(foundUser);
            logger.info("Email validation status for user {} successfully updated to {}",foundUser.getEmail(), foundUser.getIsAllowedNotification());
            return true;
        } else {
            logger.warn("No user found for email: {}", email);
            return false;
        }
    }
    public boolean emailValidationCode(String email) {
        try { 
            RegisterCode registerCode = new RegisterCode();
            registerCode.setEmail(email);
            registerCode.setExpirationTime(LocalDateTime.now().plus(5, ChronoUnit.MINUTES));
            registerCode.setCode(generateCode.generateVerificationCode());
            registerCodeRepository.save(registerCode);
            logger.info("Verification code generated and saved for email: {}", email);
            notificationServiceImpl.sendRegisterCode(registerCode.getEmail(), registerCode.getCode());
            logger.info("Verification code email sent to: {}", email);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send verification email for: {}", email, e);
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }
    }

    public boolean validateRegisterCode(String email, String code) {
        logger.info("Validating register code for email: {}", email);
        List<RegisterCode>listRegisterCode= registerCodeRepository.findByEmail(email);
        if (listRegisterCode.isEmpty()) {
            logger.warn("No verification codes found for email: {}", email);
            return false; // No tokens found for the given email
        }
        RegisterCode lastOne=listRegisterCode.get(listRegisterCode.size()-1);

        if (lastOne.getCode().equals(code) && lastOne.getEmail().equals(email)) {
            logger.info("Verification email code validated {}", email);
            return  lastOne.getExpirationTime().isAfter(LocalDateTime.now());
        }
        logger.warn("Invalid verification code for email: {}", email);
        return false;
    }

    public boolean addNewDevice(String username, String deviceToken) {
        logger.info("Starting to add new device for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found with username: " + username);
                });

        logger.info("User found: {} (ID: {})", user.getUsername(), user.getId());

        boolean tokenExists = userDeviceRepository.existsByUserIdAndDeviceToken(user.getId(), deviceToken);
        if (tokenExists) {
            logger.warn("Device token already exists for user: {} (Token: {})", username, deviceToken);
            return false; 
        }
        UserDevice userDevice = new UserDevice();
        userDevice.setUserId(user.getId());
        userDevice.setDeviceToken(deviceToken);
        userDevice.setUsername(username);
        userDeviceRepository.save(userDevice);

        logger.info("New device token successfully added for user: {} (Token: {})", username, deviceToken);
        return true; 
    }

    public boolean deleteDevice(String username, String deviceToken) {
        logger.info("Starting to delete device token for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found with username: " + username);
                });

        logger.info("User found: {} (ID: {})", user.getUsername(), user.getId());

        UserDevice userDevice = userDeviceRepository.findByUserIdAndDeviceToken(user.getId(), deviceToken)
                .orElseThrow(() -> {
                    logger.warn("Device token not found for user: {} (Token: {})", username, deviceToken);
                    return new IllegalArgumentException("Device token not found for user: " + username);
                });

        userDeviceRepository.delete(userDevice);
        logger.info("Device token successfully deleted for user: {} (Token: {})", username, deviceToken);

        return true; // Cihaz token başarıyla silindi
    }

    public List<String> getAllUsersDeviceToken(String city, String district, String excludeEmail) {
        return userDeviceRepository.findDeviceTokensByCityAndDistrict(city, district,excludeEmail );
    }


    public User mapUserDtoToUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setCity(userDto.getCity());
        user.setDistrict(userDto.getDistrict());
        user.setRole(userDto.getRole());
        user.setIsAllowedNotification(userDto.getIsAllowedNotification());
        user.setEmailValidation(userDto.getEmailValidation());
        return user;
    }

    
}


