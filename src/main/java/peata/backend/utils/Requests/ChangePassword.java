package peata.backend.utils.Requests;

import lombok.Data;

@Data
public class ChangePassword {
    String code;
    String newPassword;
    String email;


}
