package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.Country;

public interface CountryRepository extends JpaRepository<Country,Long>{
    
}
