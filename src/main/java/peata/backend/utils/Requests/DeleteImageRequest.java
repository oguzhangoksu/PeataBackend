package peata.backend.utils.Requests;

import java.util.List;

import lombok.Data;

@Data
public class DeleteImageRequest {

    private List<String> images;
    private Long addId;
    
}
