package peata.backend.utils.Requests;

import lombok.Data;

@Data
public class EmailValidationRequest {
    String code;
    String email;
}
