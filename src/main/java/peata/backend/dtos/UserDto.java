package peata.backend.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserDto {

    private String username;
    private String name;
    private String surname;
    private String password;
    private String email;
    private String phone;
    private String city;
    private String district;
    private List<Long> favoriteAdds= new ArrayList<>();
    private String role = "ROLE_USER";
    private Boolean isAllowedNotification=false;
    private Boolean emailValidation;
}
