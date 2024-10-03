package peata.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/token")
public class TokenController {

    @GetMapping("/validate")
    public ResponseEntity<String> isvalidate() {
        return ResponseEntity.ok("Token is valid");
    }
    
    

}
