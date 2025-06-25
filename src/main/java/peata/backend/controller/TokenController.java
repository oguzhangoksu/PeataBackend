package peata.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import peata.backend.utils.ResponseUtil;

import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("api/token")
public class TokenController {

    @GetMapping("/validate")
    public ResponseEntity<?> isvalidate() {
        return ResponseUtil.success("Token is valid");
    }
    
    

}
