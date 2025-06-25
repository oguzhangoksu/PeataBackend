// package peata.backend.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.dao.DataIntegrityViolationException;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.GetMapping;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import peata.backend.entity.Add;
// import peata.backend.entity.User;
// import peata.backend.service.abstracts.ActivityLogService;
// import peata.backend.service.abstracts.UserService;
// import peata.backend.utils.JwtProvider;
// import peata.backend.utils.ResponseUtil;
// import peata.backend.utils.UserPrincipal;
// import peata.backend.utils.Enums.ActivityType;
// import peata.backend.utils.Mapper.UserResponseMapper;
// import peata.backend.utils.Requests.AddComplaint;
// import peata.backend.utils.Requests.ChangePassword;
// import peata.backend.utils.Requests.EmailRequest;
// import peata.backend.utils.Requests.EmailValidationRequest;
// import peata.backend.utils.Requests.LoginRequest;
// import peata.backend.utils.Requests.UserDeleteReason;
// import peata.backend.utils.Requests.UserUpdateRequest;
// import peata.backend.utils.Responses.JwtResponse;
// import peata.backend.utils.Responses.LanguageRequest;
// import peata.backend.utils.Responses.UserResponse;
// import peata.backend.dtos.ActivityLogDto;
// import peata.backend.dtos.UserDto;

// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
// import jakarta.persistence.EntityNotFoundException;
// import io.swagger.v3.oas.annotations.Operation;
// import org.springframework.web.bind.annotation.RequestParam;

// import java.util.List;
// import java.util.Map;
// import java.util.Set;

// @RestController
// @RequestMapping("api/user")
// public class UserControllerOld {

//     private static final Logger logger = LoggerFactory.getLogger(UserController.class);

//     @Autowired
//     private UserService userService;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @Autowired
//     private JwtProvider jwtProvider;

//     @Autowired
//     private AuthenticationManager authenticationManager;

//     @Autowired
//     private UserResponseMapper userResponseMapper;

//     @Autowired
//     private ActivityLogService activityLogService;

//     @Operation(summary = "Public API", description = "Description: This endpoint allows new users to register by providing their details. It encrypts the password and saves the user in the database.")
//     @PostMapping("/auth/register")
//     public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
//         try {
//             User user = userService.mapUserDtoToUser(userDto); 
//             User savedUser = userService.save(user); 
//             logger.info("User created successfully: {}", savedUser.getUsername());
//             return ResponseEntity.ok(savedUser);
//         } catch (DataIntegrityViolationException e) {

//             String message = handleDataIntegrityViolationException(e);
//             return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
//         } catch (Exception e) {
//             logger.error("An error occurred while creating the user.", e);
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("An error occurred while creating the user.");
//         }
//     }

//     @Operation(summary = "Public API", description = "This endpoint allows users to log in by providing their identifier (username or email) and password. It authenticates the user and generates a JWT token")
//     @PostMapping("/auth/login")
//     public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
//         try {
//             Authentication authentication = authenticationManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(
//                             loginRequest.getIdentifier(),
//                             loginRequest.getPassword()));
//             System.out.println("authentication.getPrincipal():"+ authentication.getPrincipal());
//             System.out.println("loginRequest.getPassword:"+ loginRequest.getPassword());

//             SecurityContextHolder.getContext().setAuthentication(authentication);
//             String jwt = jwtProvider.generateToken(authentication);
//             logger.info("User logged in successfully: {}", loginRequest.getIdentifier());
//             return ResponseEntity.ok(new JwtResponse(jwt));
//         } catch (Exception e) {
//             logger.error("Login failed for user: {}", loginRequest.getIdentifier(), e);
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
//         }
//     }

//     @Operation(summary = "Public API", description = "Allows the user to change their preferred language. The user's identity is verified via JWT, and the language preference is updated.", security = @SecurityRequirement(name = "bearerAuth"))
//     @PostMapping("/changeLanguage")
//     public ResponseEntity<?> changeLanguage(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody LanguageRequest language) {
        
//         if(userService.changeLanguage(userPrincipal.getUsername(),language.getLanguage())){
//             logger.info("Language changed successfully for user: {}", userPrincipal.getUsername());
//             return ResponseEntity.ok("Langauge changed successfully.");
//         }
//         logger.warn("Language change failed for user: {}", userPrincipal.getUsername());
//         return ResponseEntity.badRequest().body("Language change failed.");

//     }

//     @Operation(summary = "Secured API", description = "This endpoint returns the authenticated user's information based on the JWT token provided in the request.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/getUserInformation")
//     public ResponseEntity<UserResponse> getUserInformation(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         logger.info("Fetching user information for user: {}", userPrincipal.getUsername());
//         User userDb = userService.findUserByUsername(userPrincipal.getUsername());
//         UserResponse userResponse = userResponseMapper.toResponse(userDb);
//         return ResponseEntity.ok(userResponse);
//     }

//     @Operation(summary = "Retrieve User ID", description = "This secured API retrieves the user ID of the currently authenticated user based on the JWT token provided in the Authorization header.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/getUserId")
//     public ResponseEntity<?> getUserId(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         try {
//             Long userID = userService.findUserIdByUsername(userPrincipal.getUsername());
//             logger.info("User ID {} successfully retrieved for username: {}", userID, userPrincipal.getUsername());

//             return ResponseEntity.ok(userID);
//         } catch (EntityNotFoundException ex) {

//             logger.error("User with username {} not found: {}", userPrincipal.getUsername(), ex.getMessage());
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//         } catch (Exception ex) {

//             logger.error("An unexpected error occurred while fetching User ID for username {}: {}",
//                     userPrincipal.getUsername(), ex.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
//         }
//     }

//     @Operation(summary = "Secured API", description = "This endpoint returns a set of ads associated with the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/findUsersAddsById")
//     public ResponseEntity<Set<Add>> findUsersAddsById(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         logger.info("Fetching ads for user: {}", userPrincipal.getUsername());
//         return ResponseEntity.ok(userService.findUsersAddsById(userPrincipal.getUsername()));
//     }

//     @Operation(summary = "Secured API", description = "This endpoint allows authenticated users to delete their own account.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/delete")
//     public ResponseEntity<String> delete(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         String username = userPrincipal.getUsername();
//         User userToDelete = userService.findUserByUsername(username);
//         if (!userToDelete.getUsername().equals(username)) {
//             logger.warn("User {} attempted to delete another user's account.", username);
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this user.");
//         }
//         userService.delete(userToDelete.getId());
//         logger.info("User account deleted: {}", username);
//         return ResponseEntity.ok("User deleted");
//     }

//     @Operation(summary = "Secured API", description = "This endpoint updates the notification preference for the user. It sends a notification email based on the updated status.", security = @SecurityRequirement(name = "bearerAuth"))

//     @GetMapping("/changeNotificationStatus")
//     public ResponseEntity<String> changeNotificationStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         User userDb = userService.findUserByUsername(userPrincipal.getUsername());
//         if (userService.changeNotificationStatus(userDb)) {
//             logger.info("Notification status updated for user username: {}", userDb.getUsername());
//             return ResponseEntity.ok("Notification status updated. Will be sent via mail.");
//         } else {
//             logger.info("Notification status disabled for user username: {}", userDb.getUsername());
//             return ResponseEntity
//                     .ok("Notification status updated. Receiving notifications via email has been disabled.");
//         }

//     }

//     @Operation(summary = "Secured API", description = "This endpoint allows an authenticated user to update their information, including username, name, surname, password, email, and phone. It checks for unique constraints on username and email before updating.", security = @SecurityRequirement(name = "bearerAuth"))
//     @PutMapping("/update")
//     public ResponseEntity<String> updateUserInfo(@RequestBody UserUpdateRequest userDto,
//             @AuthenticationPrincipal UserPrincipal userPrincipal) {

//         String currentUsername = userPrincipal.getUsername();

//         // Check for missing fields
//         if (userDto.getUsername() == null  ||userDto.getEmail() == null || userDto.getCity() == null || userDto.getDistrict() == null) {
//             logger.warn("Missing fields in user update request for user: {}", currentUsername);
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing fields in user update request.");
//         }

//         // Check for username and email conflicts
//         if (userService.isUsernameExist(userDto.getUsername()) && !userDto.getUsername().equals(currentUsername)) {
//             logger.warn("Username conflict for user: {} with new username: {}", currentUsername, userDto.getUsername());
//             return ResponseEntity.status(HttpStatus.CONFLICT)
//                     .body("Username already exists. Please choose a different username.");
//         }

//         if (userService.isEmailExist(userDto.getEmail()) && !userDto.getEmail().equals(userPrincipal.getEmail())) {
//             logger.warn("Email conflict for user: {} with new email: {}", currentUsername, userDto.getEmail());
//             return ResponseEntity.status(HttpStatus.CONFLICT)
//                     .body("Email already exists. Please choose a different email.");
//         }
        
//         if(userService.updateUser(currentUsername,userDto)){
//             logger.info("User information updated for user: {}", currentUsername);
//             return ResponseEntity.ok("User information updated successfully.");
//         }
//         else{
//             logger.warn("Failed to update user information for user: {}", currentUsername);
//             return ResponseEntity.badRequest().body("Failed to update user information.");
//         }
//     }

//     @Operation(summary = "Secured API", description = "This endpoint allows users to add an ad to their favorites list.", security = @SecurityRequirement(name = "bearerAuth"))

//     @GetMapping("/addFavoriteAdds")
//     public ResponseEntity<String> addFavoriteAdds(@RequestParam() Long AddId,
//             @AuthenticationPrincipal UserPrincipal userPrincipal) {
//         String username = userPrincipal.getUsername();
//         boolean isAdded = userService.addFavorite(AddId, username);
//         if (isAdded) {
//             logger.info("Ad ID: {} added to favorites by user: {}", AddId, userPrincipal.getUsername());
//             return ResponseEntity.ok("Added to favorites.");
//         } else {
//             logger.warn("Ad ID: {} does not exist for user: {}", AddId, userPrincipal.getUsername());
//             return ResponseEntity.badRequest().body("Ad is not exist anymore");
//         }

//     }

//     @Operation(summary = "Secured API", description = "This endpoint retrieves a list of IDs for the ads that the user has marked as favorites.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/getFavorites")
//     public ResponseEntity<List<Long>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         logger.info("Fetching favorite ads for user: {}", userPrincipal.getUsername());
//         String username = userPrincipal.getUsername();
//         User user = userService.findUserByUsername(username);

//         return ResponseEntity.ok(user.getFavoriteAdds());
//     }

//     @Operation(summary = "Secured API", description = "Initiates the password reset process by sending a verification code to the user's registered email or username based on the provided identifier.", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/getPasswordCode")
//     public ResponseEntity<String> getPasswordCode(@RequestParam String identifier) {
//         try {
//             logger.info("Initiating password reset for identifier: {}", identifier);
//             return ResponseEntity.ok(userService.createPaswwordResetCode(identifier));
//         } catch (Exception e) {
//             logger.error("Error initiating password reset: {}", e.getMessage());
//             return ResponseEntity.badRequest().body(e.toString());
//         }
//     }

//     @Operation(summary = "Secured API", description = "Validates the provided verification code and email, and updates the user's password if the verification is successful.", security = @SecurityRequirement(name = "bearerAuth"))
//     @PostMapping("/changePassword")
//     public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword) {
//         if (userService.validateVerificationCode(changePassword.getEmail(), changePassword.getCode())) {
//             userService.updatePassword(changePassword.getEmail(), changePassword.getNewPassword());
//             logger.info("Password changed successfully for email: {}", changePassword.getEmail());
//             return ResponseEntity.ok("User's password changed");
//         } else {
//             logger.warn("Invalid code or email for password change attempt: {}", changePassword.getEmail());
//             return ResponseEntity.badRequest().body("code or email is not valid.");
//         }

//     }

//     @Operation(summary = "Public API", description = "This endpoint verifies the user's email by validating the provided code. "
//             +
//             "If the validation succeeds, the user's email status will be updated in the system.", security = @SecurityRequirement(name = "bearerAuth"))
//     @PostMapping("/emailVerification")
//     public ResponseEntity<String> emailVerification(@RequestBody EmailValidationRequest emailValidationRequest) {
//         boolean isValid = userService.emailValidation(emailValidationRequest.getEmail(),
//                 emailValidationRequest.getCode());

//         if (isValid) {
//             logger.info("Email verification successful for email: {}", emailValidationRequest.getEmail());
//             return ResponseEntity.ok("User's email validated.");
//         } else {
//             logger.warn("Email verification failed for email: {}", emailValidationRequest.getEmail());
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email or code.");
//         }
//     }

//     @Operation(summary = "Public API", description = "This endpoint verifies the user's email by validating the provided code. "
//             +
//             "If the validation succeeds, the user's email status will be updated in the system.")
//     @PostMapping("/getEmailVerificationCode")
//     public ResponseEntity<String> getEmailVerificationCode(@RequestBody EmailRequest emailRequest) {
//         try {
//             if(emailRequest.getLanguage() == null){
//                 logger.info("No language provided in request. Defaulting language to 'tr'");
//                 emailRequest.setLanguage("tr");
//             }
//             if (userService.emailValidationCode(emailRequest.getEmail(),emailRequest.getLanguage())) {
//                 logger.info("Verification code successfully generated and sent to email: {}", emailRequest.getEmail());
//                 return ResponseEntity.ok("Verification code sent to email");
//             } else {
//                 logger.warn("Failed to generate verification code for email: {}", emailRequest.getEmail());
//                 return ResponseEntity.badRequest().body("Failed to generate verification code");
//             }
//         } catch (Exception e) {
//             logger.error("Error generating verification code: {}", e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
//         }
//     }

//     @Operation(summary = "Secured API ", description = "This API endpoint allows authenticated users to remove a specified ad from their favorites by providing its ID, with user authentication required for access. ", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/deleteFavoriteAdd")
//     public ResponseEntity<String> deleteFavoriteAdd(@RequestParam Long AddId,
//             @AuthenticationPrincipal UserPrincipal userPrincipal) {
//         String username = userPrincipal.getUsername();
//         User userDb = userService.findUserByUsername(userPrincipal.getUsername());

//         if (userService.deleteFavorite(userDb, AddId)) {
//             logger.info("Ad ID: {} successfully removed from favorites for user: {}", AddId, username);
//             return ResponseEntity.ok("Ad removed");
//         } else {
//             logger.warn("Ad ID: {} not found in favorites for user: {}", AddId, username);
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body("Ad not found in user's favorites");
//         }

//     }
//     @Operation(summary = "Secured API ", description = "This secured API allows an authenticated user to save a new device token associated with their account. ", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/userDeviceSave")
//     public ResponseEntity<String> userDeviceSave(@RequestParam String device,@AuthenticationPrincipal UserPrincipal userPrincipal) {
//         String username = userPrincipal.getUsername();
//         Boolean result = userService.addNewDevice(username,device);

//         if(result == false) {
//             logger.error("Error saving device for user: {}: {}", username,device );
//             return ResponseEntity.badRequest().body("Cihaz kaydedilirken hata oluştu.");
//         }
//         logger.info("User: {}'s device saved successfully to device: {}.", username, device);
//         return ResponseEntity.ok("Cihaz kaydedildi.");        

//     }
//     @Operation(summary = "Secured API ", description = "This secured API allows an authenticated user to delete an existing device token associated with their account. ", security = @SecurityRequirement(name = "bearerAuth"))
//     @GetMapping("/userDeviceDelete")
//     public ResponseEntity<String> userDeviceDelete(@RequestParam String device, @AuthenticationPrincipal UserPrincipal userPrincipal) {
//         String username = userPrincipal.getUsername();

//         Boolean result = userService.deleteDevice(username, device);

//         if (!result) {
//             logger.error("Error deleting device for user: {} with token: {}", username, device);
//             return ResponseEntity.badRequest().body("Cihaz silinirken hata oluştu.");
//         }

//         logger.info("Device token successfully deleted for user: {} and device: {}", username, device);
//         return ResponseEntity.ok("Cihaz başarıyla silindi.");
//     }





//     @Operation(summary = "Secured API ", 
//     description = "\n" + //
//                 "This API endpoint allows authenticated users to submit a reason for their account deletion. ", //
//     security = @SecurityRequirement(name = "bearerAuth"))   
//     @PostMapping("/delete/reason")
//     public ResponseEntity<Map<String, Object>> reason(@AuthenticationPrincipal UserPrincipal userPrincipal,UserDeleteReason userDeleteReason)  {
//         User user =userService.findUserByUsername(userPrincipal.getUsername());
//         ActivityLogDto activityLogDto = new ActivityLogDto();
//         activityLogDto.setContent(userDeleteReason.getDescription());
//         activityLogDto.setActivityType(ActivityType.DELETE_USER);
        
//         if(user != null && user.getUsername().equals(userPrincipal.getUsername())){
//             activityLogService.saveActivityLog(activityLogDto,null,null);
//             logger.info("User {} has requested account deletion with reason: {}", userPrincipal.getUsername(), userDeleteReason.getDescription());
//             return ResponseUtil.success("Your request for account deletion has been received. We will process it shortly.");
//         }
//         else{
//             logger.warn("User {} attempted to delete another user's account.", userPrincipal.getUsername());
//             return ResponseUtil.error("You are not authorized to delete this user.");
//         }
        
//     }



//     private String handleDataIntegrityViolationException(DataIntegrityViolationException e) {
//         if (e.getMessage().contains("Key (username)")) {
//             logger.error("Error creating user: Username already exists.", e.getMessage());
//             return "Kullanıcı adı alınmıştır.";
//         } else if (e.getMessage().contains("Key (email)")) {
//             logger.error("Error creating user: Email address already exists.", e.getMessage());
//             return "E-posta adresi alınmıştır.";
//         }
//         logger.error("Data integrity violation occurred.", e.getMessage());
//         return "A data integrity error occurred.";
//     }



// }
