package peata.backend.utils.Requests;

import lombok.Data;

@Data
public class SuggestionRequest {
     
    private String email;
    private String suggestion;
}
