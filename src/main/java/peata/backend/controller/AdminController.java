package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.LoginRequest;
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
    public ResponseEntity<?> createUser(@RequestBody AdminDto adminDto ,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try{
            String username = userPrincipal.getUsername();
            if(userService.findUserByUsername(username).getRole().equals("ROLE_ADMIN")){
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
            else{
                return ResponseEntity.badRequest().body("You do not have permission to add a new admin.");
            }
        } catch (DataIntegrityViolationException e) {
        // Custom error message for unique constraint violation
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username, email, or phone number already exists.");
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
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
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Perform authentication logic
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getIdentifier(), 
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generate JWT Token
        String jwt = jwtProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }




}
