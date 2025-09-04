package peata.backend.dtos;

import lombok.Data;
import peata.backend.entity.ActivityLog;
import peata.backend.utils.Enums.ActivityType;

@Data
public class ActivityLogDto {

    private Long id;
    private String content;
    private ActivityType activityType;
    
    public ActivityLog toEntity() {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setId(this.id);
        activityLog.setContent(this.content);
        activityLog.setActivityType(this.activityType);
        return activityLog;
    }
}


