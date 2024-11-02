package peata.backend.utils.Requests;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String city;
    private String district;
    private Boolean isAllowedNotification=false;

}
