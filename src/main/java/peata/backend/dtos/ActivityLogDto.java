package peata.backend.dtos;

import lombok.Data;
import peata.backend.utils.Enums.ActivityType;

@Data
public class ActivityLogDto {

    private Long id;
    private String content;
    private ActivityType activityType; 
}
