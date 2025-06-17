package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import peata.backend.dtos.ActivityLogDto;
import peata.backend.entity.ActivityLog;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.repositories.ActivityLogRepository;
import peata.backend.service.abstracts.ActivityLogService;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;


    public boolean saveActivityLog(ActivityLogDto activityLogDto,User user,Add add) {
        try {
            ActivityLog activityLog = new ActivityLog();
            if(user !=null){
                activityLog.setUser(user);
            }
            if(add !=null){
                activityLog.setAdd(add);
            }
            activityLog.setContent(activityLogDto.getContent());
            activityLog.setActivityType(activityLogDto.getActivityType());
            activityLogRepository.save(activityLog);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    
}