package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.AdminDto;
import peata.backend.entity.User;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.JwtProvider;
import peata.backend.utils.Requests.LoginRequest;
import peata.backend.utils.Responses.ErrorResponse;
import peata.backend.utils.Responses.JwtResponse;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    


    @Operation(
        summary = "Public API",
        description = "This endpoint does not require authentication."
    )
    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@RequestBody AdminDto adminDto) {
        try{
        User admin = new User();
        admin.setUsername(adminDto.getUsername());
        admin.setName(adminDto.getName());
        admin.setSurname(adminDto.getSurname());
        admin.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        admin.setEmail(adminDto.getEmail());
        admin.setPhone(adminDto.getPhone());
        admin.setCity(adminDto.getCity());
        admin.setDistrict(adminDto.getDistrict());
        admin.setRole(adminDto.getRole());
        admin.setIsAllowedNotification(adminDto.getIsAllowedNotification()); // map the field
        User adminDb=userService.save(admin);
        return ResponseEntity.ok(adminDb);
        }
        catch(Exception e){
             e.printStackTrace(); 

            // Return a meaningful error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred."));
        }

    }


    @Operation(summary = "Secured API", 
               description = "This endpoint requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth")
               ) 
    @GetMapping("/deneme")
    public ResponseEntity<String> deneme() {
        return ResponseEntity.ok("admin oldu.");
    }
    


    @Operation(
        summary = "Public API",
        description = "This endpoint does not require authentication."
    )
    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Perform authentication logic
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generate JWT Token
        String jwt = jwtProvider.generateToken(authentication);
        
        return ResponseEntity.ok(new JwtResponse(jwt));
    }




}
