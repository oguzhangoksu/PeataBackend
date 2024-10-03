package peata.backend.dtos;

import lombok.Data;

@Data
public class AdminDto {
    private String username;
    private String name;
    private String surname;
    private String password;
    private String email;
    private String phone;
    private String city;
    private String district;
    private String role = "ROLE_ADMIN";
    private Boolean isAllowedNotification=false;
}
