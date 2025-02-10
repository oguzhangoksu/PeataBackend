package peata.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.PlayerDto;
import peata.backend.entity.Player;
import peata.backend.service.abstracts.PlayerService;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Responses.PlayerUpdateRequest;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/game")
public class PlayerController {
    
    @Autowired
    private PlayerService playerService;


    @Operation(summary = "Secured API", 
        description = "This secured API endpoint returns the score of the authenticated player.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/score")
    public ResponseEntity<PlayerDto> score(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Player player = playerService.getPlayerByUsername(userPrincipal.getUsername());
        PlayerDto playerDtoResponse = new PlayerDto();
        playerDtoResponse.setUsername(player.getUsername());
        playerDtoResponse.setScore(player.getScore());

        return ResponseEntity.ok(playerDtoResponse);
    }

    @Operation(summary = "Public API", 
    description = "This public API endpoint returns the leaderboard with scores of all players.",
    security = @SecurityRequirement(name = "bearerAuth")
) 
    @GetMapping("/scoreBoard")
    public ResponseEntity<List<PlayerDto>> scoreBoard() {
        List<PlayerDto> players = playerService.getLeaderboard();
        return ResponseEntity.ok(players);
    }

    @Operation(summary = "Secured API", 
        description = "This secured API endpoint updates the score of the authenticated player.",
        security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PostMapping("/updateScore")
    public ResponseEntity<PlayerDto> updateScore(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PlayerUpdateRequest playerUpdateRequest) {
        PlayerDto playerDto = new PlayerDto();
        playerDto.setUsername(userPrincipal.getUsername());
        playerDto.setScore(playerUpdateRequest.getScore());
        Player playerDb =playerService.updateScore(playerDto);
        PlayerDto playerDtoResponse = new PlayerDto();
        playerDtoResponse.setUsername(playerDb.getUsername());
        playerDtoResponse.setScore(playerDb.getScore());
        return ResponseEntity.ok(playerDtoResponse);
    }
}
