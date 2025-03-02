package peata.backend.service.concretes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.dtos.PlayerDto;
import peata.backend.entity.Player;
import peata.backend.repositories.PlayerRepository;
import peata.backend.service.abstracts.PlayerService;
import peata.backend.service.abstracts.UserService;

import java.util.Optional;
@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserService userService;


    public List<PlayerDto> getLeaderboard() {
        List<Player> players = playerRepository.findAllByOrderByScoreAsc();
        List<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player : players) {
            PlayerDto playerDto = new PlayerDto();
            playerDto.setUsername(player.getUsername());
            playerDto.setScore(player.getScore());
            playerDtos.add(playerDto);
        }
        

        return playerDtos;
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).get();
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }
    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username);
    }

 
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }
   
    public Player updateScore(PlayerDto playerDto) {
        Optional<Player> existingPlayerOpt = Optional.ofNullable(playerRepository.findByUsername(playerDto.getUsername()));
        if(existingPlayerOpt.isPresent()){
            Player existingPlayer = existingPlayerOpt.get();
            if( Integer.parseInt(existingPlayer.getScore()) < Integer.parseInt(playerDto.getScore())){
                existingPlayer.setScore(playerDto.getScore());
                return playerRepository.save(existingPlayer);
            }
            else{
                return existingPlayer;
            }
        } else {
            Player player = new Player();
            player.setUsername(playerDto.getUsername());
            player.setUserId(userService.findUserByUsername(playerDto.getUsername()).getId());
            player.setScore(playerDto.getScore());
            return playerRepository.save(player);
        }
    }
   
    
}
