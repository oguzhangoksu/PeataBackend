package peata.backend.service.abstracts;

import java.util.List;

import org.springframework.data.domain.Page;

import peata.backend.dtos.UserDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.utils.Requests.UserUpdateRequest;

import java.util.Set;

public interface UserService {
    public User save(User user);
    public boolean addFavorite(Long AddId,String username);
    public void delete(Long id);
    public List<User> allUsers();
    public User findUserById(Long id);
    public Set<Add> findUsersAddsById(String username);
    public Page<User> getPaginatedUsers(int page, int size);
    public boolean changeNotificationStatus(User user);
    public boolean isUsernameExist(String username);
    public boolean isEmailExist(String email);
    public List<String>findEmailsByCityAndDistrict(String city, String email, String publisherEmail);
    public List<String> findEmailsByCityAndDistrictOnValidateEmail(String city,String email,String publisherEmail,String language);
    public User findUserByUsername(String username);
    public String createPaswwordResetCode(String identifier);
    public boolean validateVerificationCode(String email, String code);
    public void updatePassword(String email, String newPassword);
    public boolean deleteFavorite(User user,Long AddId);
    public User mapUserDtoToUser(UserDto userDto);
    public Long findUserIdByUsername(String username);
    public boolean validateRegisterCode(String email, String code);
    public boolean emailValidation(String email, String code);
    public boolean emailValidationCode(String email,String language);
    public Boolean updateUser(String currentUsername,UserUpdateRequest user);
    public boolean addNewDevice(String username, String deviceToken);
    public boolean deleteDevice(String username, String deviceToken);
    public List<String> getAllUsersDeviceToken(String city, String district, String excludeEmail, String language);
    public boolean changeLanguage(String username, String language);
} 
