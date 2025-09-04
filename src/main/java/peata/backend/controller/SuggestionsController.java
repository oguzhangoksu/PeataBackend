package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.ActivityLogDto;
import peata.backend.entity.ActivityLog;
import peata.backend.entity.User;
import peata.backend.service.abstracts.ActivityLogService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;

@RestController
@RequestMapping("api/suggestions")
public class SuggestionsController {
 
    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Public API", description = "Allows users to submit suggestions.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestBody ActivityLogDto activityLogDto , @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.findUserById(userService.findUserIdByUsername(userPrincipal.getUsername()));
            ActivityLog activityLog = activityLogDto.toEntity();
            activityLogService.saveActivityLog(activityLogDto, user);

            if (activityLog != null) {
                return ResponseUtil.success("Öneri başarıyla gönderildi.");
            } else {
                return ResponseUtil.error("Öneri gönderilemedi, lütfen tekrar deneyin.");
            }
        } catch (Exception e) {
            return ResponseUtil.error("Sunucu hatası: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
