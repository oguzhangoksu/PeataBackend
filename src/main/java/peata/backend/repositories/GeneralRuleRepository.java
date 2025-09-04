package peata.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import peata.backend.entity.GeneralRule;

public interface GeneralRuleRepository extends JpaRepository<GeneralRule,Long>{

    Optional<GeneralRule> findByVersion_VersionAndIsActiveTrue(String version);
}
