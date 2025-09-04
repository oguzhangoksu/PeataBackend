package peata.backend.service.abstracts;

import peata.backend.dtos.ActivityLogDto;
import peata.backend.entity.Add;
import peata.backend.entity.Binding;
import peata.backend.entity.User;

public interface ActivityLogService {
    public boolean saveActivityLog(ActivityLogDto activityLogDto,User user,Add add);
    public boolean saveActivityLog(ActivityLogDto activityLogDto,User user);
    public boolean saveActivityLogBinding(ActivityLogDto activityLogDto, User user, Binding binding);
}
