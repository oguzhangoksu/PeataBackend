package peata.backend.utils.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AddResponse {

    private String message;
    private Long add_id;
    private List<String> images;
}
