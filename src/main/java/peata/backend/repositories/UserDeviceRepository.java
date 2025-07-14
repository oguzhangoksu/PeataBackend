package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import peata.backend.entity.UserDevice;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice,Long>{

    List<UserDevice> findByUserId(Long userId);
    boolean existsByUserIdAndDeviceToken(Long userId, String deviceToken);
    Optional<UserDevice> findByUserIdAndDeviceToken(Long userId, String deviceToken);
    //değiştirildi
    @Query("SELECT ud.deviceToken FROM UserDevice ud WHERE ud.userId IN " +
       "(SELECT u.id FROM User u WHERE u.city = :city AND u.district = :district AND u.email <> :excludeEmail)")
    List<String> findDeviceTokensByCityAndDistrict(String city, String district, String excludeEmail);
} 

