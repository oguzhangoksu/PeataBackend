package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import peata.backend.entity.UserDevice;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice,Long>{

    List<UserDevice> findByUserId(Long userId);
    boolean existsByUserIdAndDeviceToken(Long userId, String deviceToken);
    Optional<UserDevice> findByUserIdAndDeviceToken(Long userId, String deviceToken);
} 