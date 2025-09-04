package peata.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import peata.backend.entity.Add;
import java.util.List;



public interface AddRepository extends JpaRepository<Add,Long>{

    Page<Add> findByStatus(int status,PageRequest pageRequest);
    List<Add> findByPcode(String pcode);
    List<Add> findByCountryId(int countryId);
    Page<Add> findByCountryId(int countryId,PageRequest pageRequest);
    
    @Query("SELECT a FROM Add a WHERE a.isActive = :active AND a.countryId = :countryId ORDER BY a.currentDate DESC")
    Page<Add> findActiveByCountry(@Param("active") boolean active, @Param("countryId") int countryId, Pageable pageable);
    
    @Query("SELECT a FROM Add a WHERE a.isActive = :active ORDER BY a.currentDate DESC")
    Page<Add> findAllActive(@Param("active") boolean active, Pageable pageable);
}
