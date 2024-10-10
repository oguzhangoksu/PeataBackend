package peata.backend.utils.Requests;

import lombok.Data;

@Data
public class LoginRequest {
    
    private String identifier;
    private String password;

}
