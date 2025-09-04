package peata.backend.utils.Requests;

import lombok.Data;
import peata.backend.utils.Enums.ActivityType;

@Data
public class ChatComplaintRequest {

    private String content;
    private ActivityType activityType = ActivityType.COMPLAINT;
    private Long bindingId;

}
