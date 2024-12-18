package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import peata.backend.entity.Suggestions;

@Repository
public interface SuggestionsRepository extends JpaRepository<Suggestions,Long>{

    
}
