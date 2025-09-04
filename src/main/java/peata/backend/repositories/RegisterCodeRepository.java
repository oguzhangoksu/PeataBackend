package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import peata.backend.entity.RegisterCode;
import java.util.List;




@Repository
public interface RegisterCodeRepository extends JpaRepository<RegisterCode,Long> {
    
    List<RegisterCode> findByEmail(String email);
    List<RegisterCode> findByEmailOrderByExpirationTimeAsc(String email);
    void deleteByEmail(String email);

}
