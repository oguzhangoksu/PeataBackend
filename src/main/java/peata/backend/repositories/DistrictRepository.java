package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import peata.backend.entity.District;
import java.util.List;


public interface DistrictRepository extends JpaRepository<District,Long>{

    @Query(value = "SELECT * FROM districts WHERE city_id = ?1", nativeQuery = true)
    List<District> findByCityId(Long id);

    @Query(value = "SELECT * FROM districts WHERE country_id = ?1", nativeQuery = true)
    List<District> findByCountryId(Long id);
} 