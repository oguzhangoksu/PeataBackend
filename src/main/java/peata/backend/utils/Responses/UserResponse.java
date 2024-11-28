package peata.backend.utils.Responses;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import lombok.Data;
import peata.backend.entity.Add;

@Data
public class UserResponse {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String city;
    private String district;
    private Boolean isAllowedNotification;
    private Boolean emailValidation;
    private List<Long> favoriteAdds;
    private Set<Add> ads = new HashSet<>();
}   
