package peata.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.entity.Suggestions;
import peata.backend.service.abstracts.SuggestionsService;
import peata.backend.service.concretes.UserServiceImpl;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.SuggestionRequest;

@RestController
@RequestMapping("/suggestions")
public class SuggestionsController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private SuggestionsService suggestionService;

    @Operation(summary = "Public API", description = "Allows users to submit suggestions.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/suggest")
    public ResponseEntity<String> suggest(SuggestionRequest suggestionRequest) {
        try {
            Suggestions suggestion = suggestionService.save(suggestionRequest);
            if (suggestion != null) {
                return ResponseEntity.ok("Öneri başarıyla gönderildi.");
            } else {
                return ResponseEntity.badRequest().body("Öneri gönderilemedi, lütfen tekrar deneyin.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sunucu hatası: " + e.getMessage());
        }
    }
    
}
