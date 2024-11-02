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
import org.springframework.web.bind.annotation.GetMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.JwtProvider;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Mapper.UserResponseMapper;
import peata.backend.utils.Requests.ChangePassword;
import peata.backend.utils.Requests.LoginRequest;
import peata.backend.utils.Requests.UserUpdateRequest;
import peata.backend.utils.Responses.JwtResponse;
import peata.backend.utils.Responses.UserResponse;
import peata.backend.dtos.UserDto;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;



@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserResponseMapper userResponseMapper;



    
    @Operation(
        summary = "Public API",
        description = "Description: This endpoint allows new users to register by providing their details. It encrypts the password and saves the user in the database."
    )
    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        logger.info("Creating user with username: {}", userDto.getUsername());
        try {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setName(userDto.getName());
            user.setSurname(userDto.getSurname());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setCity(userDto.getCity());
            user.setDistrict(userDto.getDistrict());
            user.setRole(userDto.getRole());
            user.setIsAllowedNotification(userDto.getIsAllowedNotification()); // map the field

            User userdb = userService.save(user);
            logger.info("User created successfully: {}", userdb.getUsername());
            return ResponseEntity.ok(userdb);
        } catch (DataIntegrityViolationException e) {
            // Custom error message for unique constraint violation
            logger.error("Error creating user: Username, email, or phone number already exists.", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username, email, or phone number already exists.");
        } catch (Exception e) {
            // Handle other exceptions
            logger.error("An error occurred while creating the user.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
        }
    }


    @Operation(
        summary = "Public API",
        description = "This endpoint allows users to log in by providing their identifier (username or email) and password. It authenticates the user and generates a JWT token"
    )
    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        logger.info("User attempting to log in: {}", loginRequest.getIdentifier());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getIdentifier(), 
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtProvider.generateToken(authentication);
            logger.info("User logged in successfully: {}", loginRequest.getIdentifier());
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginRequest.getIdentifier(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        }
    }

    @Operation(summary = "Secured API", 
               description = "This endpoint returns the authenticated user's information based on the JWT token provided in the request.",
               security = @SecurityRequirement(name = "bearerAuth")
               )       
    @GetMapping("/getUserInformation")
    public ResponseEntity<UserResponse> getUserInformation(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        logger.info("Fetching user information for user: {}", userPrincipal.getUsername());
        User userDb=userService.findUserByUsername(userPrincipal.getUsername()); 
        UserResponse userResponse =userResponseMapper.toResponse(userDb);
        return ResponseEntity.ok(userResponse);
    }
    


    @Operation(summary = "Secured API", 
    description = "This endpoint returns a set of ads associated with the currently authenticated user.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/findUsersAddsById")
    public ResponseEntity<Set<Add>> findUsersAddsById(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        logger.info("Fetching ads for user: {}", userPrincipal.getUsername());
        return ResponseEntity.ok(userService.findUsersAddsById(userPrincipal.getUsername()));
    }

    @Operation(summary = "Secured API", 
        description = "This endpoint allows authenticated users to delete their own account.",
        security = @SecurityRequirement(name = "bearerAuth")
    )     
    @GetMapping("/delete")
    public ResponseEntity<String> delete(@AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        logger.info("Attempting to delete user account: {}", username);
        User userToDelete = userService.findUserByUsername(username);
        if (!userToDelete.getUsername().equals(username)) {
            logger.warn("User {} attempted to delete another user's account.", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this user.");
        }
        userService.delete(userToDelete.getId());
        logger.info("User account deleted: {}", username);
        return ResponseEntity.ok("User deleted");
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint updates the notification preference for the user. It sends a notification email based on the updated status.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/changeNotificationStatus")
    public ResponseEntity<String> changeNotificationStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User userDb =userService.findUserByUsername(userPrincipal.getUsername());
        logger.info("Changing notification status for user username: {}", userDb.getUsername());
        if(userService.changeNotificationStatus(userDb)){
            logger.info("Notification status updated for user username: {}", userDb.getUsername());
            return ResponseEntity.ok("Notification status updated. Will be sent via mail.");
        }
        else{
            logger.info("Notification status disabled for user username: {}", userDb.getUsername());
            return ResponseEntity.ok("Notification status updated. Receiving notifications via email has been disabled.");
        }
        
    }


    @Operation(summary = "Secured API", 
    description = "This endpoint allows an authenticated user to update their information, including username, name, surname, password, email, and phone. It checks for unique constraints on username and email before updating.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PutMapping("/update")
    public ResponseEntity<String> postMethodName(@RequestBody UserUpdateRequest userDto, @AuthenticationPrincipal UserPrincipal userPrincipal ) {
        logger.info("Updating user information for user: {}", userPrincipal.getUsername());

        String currentUsername = userPrincipal.getUsername();

        // Check for missing fields
        if (userDto.getUsername() == null || userDto.getName() == null || userDto.getSurname() == null || 
            userDto.getEmail() == null || userDto.getPhone() == null || userDto.getCity() == null) {
            logger.warn("Missing fields in user update request for user: {}", currentUsername);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing fields in user update request.");
        }

        // Check for username and email conflicts
        if (userService.isUsernameExist(userDto.getUsername()) && !userDto.getUsername().equals(currentUsername)) {
            logger.warn("Username conflict for user: {} with new username: {}", currentUsername, userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists. Please choose a different username.");
        }

        if (userService.isEmailExist(userDto.getEmail()) && !userDto.getEmail().equals(userPrincipal.getEmail())) {
            logger.warn("Email conflict for user: {} with new email: {}", currentUsername, userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists. Please choose a different email.");
        }

        // Proceed with updating user information
        User userDb = userService.findUserByUsername(currentUsername);
        userDb.setUsername(userDto.getUsername());
        userDb.setName(userDto.getName());
        userDb.setSurname(userDto.getSurname());
        userDb.setEmail(userDto.getEmail());
        userDb.setPhone(userDto.getPhone());
        userDb.setCity(userDto.getCity());
        userDb.setDistrict(userDto.getDistrict()); 
        userService.save(userDb);

        logger.info("User information updated for user: {}", currentUsername);
        return ResponseEntity.ok("User information updated successfully.");
    }


    @Operation(summary = "Secured API", 
    description = "This endpoint allows users to add an ad to their favorites list.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/addFavoriteAdds")
    public ResponseEntity<String> addFavoriteAdds(@RequestParam() Long AddId,@AuthenticationPrincipal UserPrincipal userPrincipal ) {
        logger.info("User {} is adding ad ID: {} to favorites.", userPrincipal.getUsername(), AddId);
        String username = userPrincipal.getUsername();
        boolean isAdded = userService.addFavorite(AddId, username);
        if(isAdded){
            logger.info("Ad ID: {} added to favorites by user: {}", AddId, userPrincipal.getUsername());
            return ResponseEntity.ok("Added to favorites.");
        }
        else{
            logger.warn("Ad ID: {} does not exist for user: {}", AddId, userPrincipal.getUsername());
            return ResponseEntity.badRequest().body("Ad is not exist anymore");
        }
        
        
        
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint retrieves a list of IDs for the ads that the user has marked as favorites.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @GetMapping("/getFavorites")
    public ResponseEntity<List<Long>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        logger.info("Fetching favorite ads for user: {}", userPrincipal.getUsername());
        String username = userPrincipal.getUsername();
        User user =userService.findUserByUsername(username);
        
        return ResponseEntity.ok(user.getFavoriteAdds());
    }

    @Operation(summary = "Secured API", 
    description = "Initiates the password reset process by sending a verification code to the user's registered email or username based on the provided identifier.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @GetMapping("/getPasswordCode")
    public ResponseEntity<String> getPasswordCode(@RequestParam String identifier) {
        try{
            logger.info("Initiating password reset for identifier: {}", identifier);
            return ResponseEntity.ok(userService.createPaswwordResetCode(identifier));
        }
        catch(Exception e  ){
            logger.error("Error initiating password reset: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.toString());
        }
    }

    @Operation(summary = "Secured API", 
    description = "Validates the provided verification code and email, and updates the user's password if the verification is successful.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PostMapping("/changePassword")
    public ResponseEntity<String> getPasswordCode(@RequestBody ChangePassword changePassword){
        logger.info("Changing password for email: {}", changePassword.getEmail());
        if(userService.validateVerificationCode(changePassword.getEmail(),changePassword.getCode())){
            userService.updatePassword(changePassword.getEmail(), changePassword.getNewPassword());
            logger.info("Password changed successfully for email: {}", changePassword.getEmail());
            return ResponseEntity.ok("User's password changed");
        }
        else{
            logger.warn("Invalid code or email for password change attempt: {}", changePassword.getEmail());
            return ResponseEntity.badRequest().body("code or email is not valid.");
        }
    
    }

    @Operation(summary = "Secured API ", 
    description = "This API endpoint allows authenticated users to remove a specified ad from their favorites by providing its ID, with user authentication required for access. ",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/deleteFavoriteAdd")
    public ResponseEntity<String> deleteFavoriteAdd(@RequestParam Long AddId,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String username = userPrincipal.getUsername();
        User userDb=userService.findUserByUsername(userPrincipal.getUsername());
        logger.info("User {} is attempting to remove ad ID: {} from favorites.", username, AddId);

        if(userService.deleteFavorite(userDb, AddId)){
            logger.info("Ad ID: {} successfully removed from favorites for user: {}", AddId, username);
            return ResponseEntity.ok("Ad removed");
        }
        else{
            logger.warn("Ad ID: {} not found in favorites for user: {}", AddId, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Ad not found in user's favorites");
        }
       
    }
        

}

    
    

