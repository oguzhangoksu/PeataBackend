package peata.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.ActivityLog;


public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long>{
    
}
