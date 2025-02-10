package peata.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.Player;
import java.util.List;


public interface PlayerRepository extends JpaRepository<Player,Long>{
    
    public Player findByUserId(Long userId);

    
    public Player findByUsername(String username);

    public List<Player> findAllByOrderByScoreAsc();
}
