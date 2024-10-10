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
import peata.backend.utils.Requests.LoginRequest;
import peata.backend.utils.Responses.JwtResponse;
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




    
    @Operation(
        summary = "Public API",
        description = "This endpoint does not require authentication."
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
        description = "This endpoint does not require authentication."
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
               description = "This endpoint requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth")
               )       
    @GetMapping("/getUsersWithPagination")
    public ResponseEntity<Page<User>> getUsersWithPagination(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getPaginatedUsers(page, size));
    }


    @Operation(summary = "Secured API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/findUsersAddsById")
    public ResponseEntity<Set<Add>> findUsersAddsById(@RequestParam() Long id) {
        return ResponseEntity.ok(userService.findUsersAddsById(id));
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )     
    @GetMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam() Long id, @AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        User userToDelete = userService.findUserById(id);
        if (!userToDelete.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this user.");
        }
        userService.delete(id);
        return ResponseEntity.ok("User deleted");
    }

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
    /*@AuthenticationPrincipa UserPrincipal userPrincipal */
    @Operation(summary = "Secured API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  

    @GetMapping("/addFavoriteAdds")
    public ResponseEntity<String> addFavoriteAdds(@RequestParam() Long AddId,@AuthenticationPrincipal UserPrincipal userPrincipal ) {
        String username = userPrincipal.getUsername();
        
        userService.addFavorite(AddId, username);
        
        
        return ResponseEntity.ok("Added to favorites.");
    }

    @Operation(summary = "Secured API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )  
    @GetMapping("/getFavorites")
    public ResponseEntity<List<Long>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String username = userPrincipal.getUsername();
        User user =userService.findUserByUsername(username);
        
        return ResponseEntity.ok(user.getFavoriteAdds());
    }

        

}

    
    

