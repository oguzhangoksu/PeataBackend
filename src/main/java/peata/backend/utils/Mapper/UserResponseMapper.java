package peata.backend.utils.Mapper;

import org.springframework.stereotype.Component;

import peata.backend.entity.User;
import peata.backend.utils.Responses.UserResponse;

@Component
public class UserResponseMapper {
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCity(user.getCity());
        response.setDistrict(user.getDistrict());
        response.setIsAllowedNotification(user.getIsAllowedNotification());
        response.setFavoriteAdds(user.getFavoriteAdds());
        response.setAds(user.getAds());
        return response;
    }
}
