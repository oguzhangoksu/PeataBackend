package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.dtos.PlayerDto;
import peata.backend.entity.Player;

public interface PlayerService {
    public List<PlayerDto> getLeaderboard();
    public Player getPlayerById(Long id);
    public Player save(Player player);
    public Player getPlayerByUsername(String username);
    public Player updateScore(PlayerDto playerDto);
} 
