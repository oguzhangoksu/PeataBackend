package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import peata.backend.entity.City;
import java.util.List;


public interface CityRepository extends JpaRepository<City,Long> {

   @Query(value = "SELECT * FROM cities WHERE country_id = ?1", nativeQuery = true)
    List<City> findByCountryId(Long id);
    
}
