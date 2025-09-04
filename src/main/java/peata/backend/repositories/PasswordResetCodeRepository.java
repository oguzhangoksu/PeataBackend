package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.PasswordResetCode;
import java.util.List;




public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode,Long>{
    List<PasswordResetCode> findByEmail(String email);
    List<PasswordResetCode> findByEmailOrderByExpirationTimeAsc(String email);
    void deleteByEmail(String email);
}
