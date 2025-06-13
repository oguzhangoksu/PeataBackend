package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import peata.backend.entity.Version;


public interface VersionRepository extends JpaRepository<Version,Long> {

    @Query(value = "SELECT * FROM versions WHERE  version = ?1", nativeQuery = true)
    Version findByVersion(String version);
}
