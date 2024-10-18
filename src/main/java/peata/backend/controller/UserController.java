package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.JwtProvider;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Mapper.UserResponseMapper;
import peata.backend.utils.Requests.ChangePassword;
import peata.backend.utils.Requests.LoginRequest;
import peata.backend.utils.Responses.JwtResponse;
import peata.backend.utils.Responses.UserResponse;
import peata.backend.dtos.UserDto;


import org.springframework.web.bind.annotation.PostMapping;
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
        return ResponseEntity.ok(userdb);
    } catch (DataIntegrityViolationException e) {
        // Custom error message for unique constraint violation
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Username, email, or phone number already exists.");
    } catch (Exception e) {
        // Handle other exceptions
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
    }
    }


    @Operation(
        summary = "Public API",
        description = "This endpoint allows users to log in by providing their identifier (username or email) and password. It authenticates the user and generates a JWT token"
    )
    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
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

    @Operation(summary = "Secured API", 
               description = "This endpoint returns the authenticated user's information based on the JWT token provided in the request.",
               security = @SecurityRequirement(name = "bearerAuth")
               )       
    @GetMapping("/getUserInformation")
    public ResponseEntity<UserResponse> getUserInformation(@AuthenticationPrincipal UserPrincipal userPrincipal) {
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
        return ResponseEntity.ok(userService.findUsersAddsById(userPrincipal.getUsername()));
    }

    @Operation(summary = "Secured API", 
        description = "This endpoint allows authenticated users to delete their own account.",
        security = @SecurityRequirement(name = "bearerAuth")
    )     
    @GetMapping("/delete")
    public ResponseEntity<String> delete(@AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        User userToDelete = userService.findUserByUsername(username);
        if (!userToDelete.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this user.");
        }
        userService.delete(userToDelete.getId());
        return ResponseEntity.ok("User deleted");
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint updates the notification preference for the user. It sends a notification email based on the updated status.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/changeNotificationStatus")
    public ResponseEntity<String> changeNotificationStatus(@RequestParam() Long id) {
        User user =userService.findUserById(id);
        if(userService.changeNotificationStatus(user)){
            return ResponseEntity.ok("Notification status updated. Will be sent via mail.");
        }
        else{
            return ResponseEntity.ok("Notification status updated. Receiving notifications via email has been disabled.");
        }
        
    }


    @Operation(summary = "Secured API", 
    description = "This endpoint allows an authenticated user to update their information, including username, name, surname, password, email, and phone. It checks for unique constraints on username and email before updating.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PostMapping("/update")
    public ResponseEntity<String> postMethodName(@RequestBody UserDto userDto,@RequestParam() Long id, @AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        User userToDelete = userService.findUserById(id);
        if (!userToDelete.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this user.");
        }
        if( userToDelete.getUsername() != userDto.getUsername() && userService.isUsernameExist(userDto.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists. Please choose a different username.");
        }
        if(userToDelete.getEmail() != userDto.getEmail() && userService.isEmailExist(userDto.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists. Please choose a different email.");
        }

        User userDb=userService.findUserById(id);
        userDb.setUsername(userDto.getUsername());
        userDb.setName(userDto.getName());
        userDb.setSurname(userDto.getSurname());
        userDb.setPassword(userDto.getPassword());
        userDb.setEmail(userDto.getEmail());
        userDb.setPhone(userDto.getPhone());
        userDb.setCity(userDto.getCity());
        userService.save(userDb);
        return ResponseEntity.ok("User saved.");
    }


    @Operation(summary = "Secured API", 
    description = "This endpoint allows users to add an ad to their favorites list.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/addFavoriteAdds")
    public ResponseEntity<String> addFavoriteAdds(@RequestParam() Long AddId,@AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        if(userService.addFavorite(AddId, username)){
            return ResponseEntity.ok("Added to favorites.");
        }
        else{
            return ResponseEntity.badRequest().body("Ad is not exist anymore");
        }
        
        
        
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint retrieves a list of IDs for the ads that the user has marked as favorites.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @GetMapping("/getFavorites")
    public ResponseEntity<List<Long>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
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
        return ResponseEntity.ok(userService.createPaswwordResetCode(identifier));
        }
        catch(Exception e  ){
            return ResponseEntity.badRequest().body(e.toString());
        }
    }

    @Operation(summary = "Secured API", 
    description = "Validates the provided verification code and email, and updates the user's password if the verification is successful.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @PostMapping("/changePassword")
    public ResponseEntity<String> getPasswordCode(@RequestBody ChangePassword changePassword){
        if(userService.validateVerificationCode(changePassword.getEmail(),changePassword.getCode())){
            userService.updatePassword(changePassword.getEmail(), changePassword.getNewPassword());
            return ResponseEntity.ok("User's password changed");
        }
        else{
            return ResponseEntity.badRequest().body("code or email is not valid.");
        }
    
    }


        

}

    
    

