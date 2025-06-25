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
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Responses.PlayerUpdateRequest;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("api/game")
public class PlayerController {
    
    @Autowired
    private PlayerService playerService;


    @Operation(summary = "Secured API", 
        description = "This secured API endpoint returns the score of the authenticated player.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/score")
    public ResponseEntity<?> score(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Player player = playerService.getPlayerByUsername(userPrincipal.getUsername());
            if (player == null) {
                return ResponseUtil.error("Player not found.");
            }
            PlayerDto playerDtoResponse = new PlayerDto();
            playerDtoResponse.setUsername(player.getUsername());
            playerDtoResponse.setScore(player.getScore());
            return ResponseUtil.success("Player score fetched successfully.", playerDtoResponse);
        } catch (Exception e) {
            return ResponseUtil.error("Player score could not be fetched.");
        }
    }

    @Operation(summary = "Public API", 
    description = "This public API endpoint returns the leaderboard with scores of all players.",
    security = @SecurityRequirement(name = "bearerAuth")
) 
    @GetMapping("/scoreBoard")
    public ResponseEntity<?> scoreBoard() {
        try {
            List<PlayerDto> players = playerService.getLeaderboard();
            return ResponseUtil.success("Leaderboard fetched successfully.", players);
        } catch (Exception e) {
            return ResponseUtil.error("Leaderboard could not be fetched.");
        }
    }

    @Operation(summary = "Secured API", 
        description = "This secured API endpoint updates the score of the authenticated player.",
        security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PostMapping("/updateScore")
    public ResponseEntity<?> updateScore(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PlayerUpdateRequest playerUpdateRequest) {
        try {
            PlayerDto playerDto = new PlayerDto();
            playerDto.setUsername(userPrincipal.getUsername());
            playerDto.setScore(playerUpdateRequest.getScore());
            Player playerDb = playerService.updateScore(playerDto);
            PlayerDto playerDtoResponse = new PlayerDto();
            playerDtoResponse.setUsername(playerDb.getUsername());
            playerDtoResponse.setScore(playerDb.getScore());
            return ResponseUtil.success("Player score updated successfully.", playerDtoResponse);
        } catch (Exception e) {
            return ResponseUtil.error("Player score could not be updated.");
        }
    }
}
