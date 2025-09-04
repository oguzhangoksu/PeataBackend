package peata.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.ActivityLogDto;
import peata.backend.dtos.BindingDto;
import peata.backend.dtos.BindingOwnedDto;
import peata.backend.dtos.ChatMessageDto;
import peata.backend.entity.Binding;
import peata.backend.entity.ChatMessage;
import peata.backend.entity.GeneralRule;
import peata.backend.entity.User;
import peata.backend.service.abstracts.ActivityLogService;
import peata.backend.service.abstracts.BindingService;
import peata.backend.service.abstracts.ChatMessageService;
import peata.backend.service.abstracts.UserBindingSettingsService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.BlockRequest;
import peata.backend.utils.Requests.ChatComplaintRequest;
import peata.backend.utils.Requests.SendMessageRequest;
import peata.backend.utils.Requests.VersionRequest;


@RestController()
@RequestMapping("api")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private BindingService bindingService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserBindingSettingsService userBindingSettingsService;


    @GetMapping("/chat/initiate/{addId}")
    public ResponseEntity<?> initiateChat(@PathVariable Long addId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User sender = userService.findUserByUsername(userPrincipal.getUsername());
            if (sender == null) {
                return ResponseUtil.error("Sender not found.");
            }

            Binding binding = bindingService.findByIdOrCreate(addId, sender.getId());
            if (binding == null) {
                return ResponseUtil.error("Failed to create or retrieve binding.");
            }

            String blockError = chatMessageService.checkBlockStatus(sender, binding);
            if (blockError != null) {
                return ResponseUtil.error(blockError);
            }

            User owner = userService.findUserById(binding.getOwnerId());
            if (owner == null) {
                return ResponseUtil.error("Owner not found.");
            }

            userBindingSettingsService.save(binding, sender);
            userBindingSettingsService.save(binding, owner);
            
            BindingDto bindingDto = new BindingDto().convertFromEntity(binding);
            return ResponseUtil.success("Chat initiated successfully.", bindingDto);
            
        } catch (Exception e) {
            return ResponseUtil.error("An error occurred while initiating chat.");
        }
    }

    @PostMapping("/chat/sendMessage/{bindingId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long bindingId, @AuthenticationPrincipal UserPrincipal userPrincipal,@RequestBody SendMessageRequest request) {
        User sender = userService.findUserById(userPrincipal.getId());
        Binding binding = bindingService.findById(bindingId);
        if(sender.getId().equals(binding.getOwnerId()) && 
                    sender.getBlockedUsers().stream().anyMatch(user -> user.getId().equals(binding.getRequesterId()))) {
            return ResponseUtil.error("You have blocked this user.");
        }
        else if(sender.getId().equals(binding.getRequesterId()) && 
                sender.getBlockedUsers().stream().anyMatch(user -> user.getId().equals(binding.getOwnerId()))) {
            return ResponseUtil.error("You have blocked this user.");
        }
        chatMessageService.save(binding,sender.getId(), request.getMessage());
        return ResponseUtil.success("Message sent successfully.");
    }

    @PostMapping("/chat/mute/{bindingId}")
    public ResponseEntity<?> muteUser(@PathVariable Long bindingId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User sender = userService.findUserById(userPrincipal.getId());
        Binding binding = bindingService.findById(bindingId);
        if(userBindingSettingsService.muteBinding(binding, sender)){
            return ResponseUtil.success("User muted successfully.");
        }
        else{
            return ResponseUtil.error("Failed to mute user.");
        }
    }

    @PostMapping("/chat/unmute/{bindingId}")
    public ResponseEntity<?> unmuteUser(@PathVariable Long bindingId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User sender = userService.findUserById(userPrincipal.getId());
        Binding binding = bindingService.findById(bindingId);
        if(userBindingSettingsService.unmuteBinding(binding, sender)){
            return ResponseUtil.success("User unmuted successfully.");
        }
        else{
            return ResponseUtil.error("Failed to mute user.");
        }
    }

    @GetMapping("/chat/all")
    public ResponseEntity<?> getAllChat( @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User senderOrOwner = userService.findUserByUsername(userPrincipal.getUsername());
        Long senderId = senderOrOwner.getId();
        List<BindingOwnedDto> allChat = bindingService.findAllBinding(senderId);
        if (allChat.isEmpty()) {
            return ResponseUtil.error("No chats found.");
        }
        return ResponseUtil.success("All chats retrieved successfully.", allChat);
    }

    @GetMapping("/messages/{bindingId}")
    public ResponseEntity<?> getAllMessages(@PathVariable Long bindingId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ChatMessage> messages = chatMessageService.getMessagesByBindingId(bindingId);
        if (messages.isEmpty()) {
            return ResponseUtil.error("No messages found.");
        }
        
        List<ChatMessageDto> messageDtos = messages.stream()
            .map(ChatMessageDto::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseUtil.success("All messages retrieved successfully.", messageDtos);
    }

    @GetMapping("/messages/generalRules")
    public ResponseEntity<?> getGeneralRules(VersionRequest versionRequest,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        GeneralRule generalRule = chatMessageService.getActiveGeneralRule(versionRequest.getVersion());
        if(generalRule == null) {
            return ResponseUtil.error("No active general rule found.");
        }
        return ResponseUtil.success(generalRule.getGeneralRule());
    }


    @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows authenticated users to block another user. ", //
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/block")
    public ResponseEntity<Map<String, Object>> block(@AuthenticationPrincipal UserPrincipal userPrincipal,@RequestBody BlockRequest blockRequest)  {
        User userBlocker =userService.findUserById(userPrincipal.getId());
        User userBlocked = userService.findUserById(blockRequest.getId());
        if(userService.blockUser(userBlocker, userBlocked)){
             return ResponseUtil.success("User successfully blocked.");
        }
        else{
            return ResponseUtil.error("Failed to block user.");
        }
    }

    @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows authenticated users to unblock another user. ", //
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/unblock")
    public ResponseEntity<Map<String, Object>> unblock(@AuthenticationPrincipal UserPrincipal userPrincipal,@RequestBody BlockRequest blockRequest)  {
        User userBlocker =userService.findUserById(userPrincipal.getId());
        User userBlocked = userService.findUserById(blockRequest.getId());
        if(userService.unblockUser(userBlocker, userBlocked)){
             return ResponseUtil.success("User successfully unblocked.");
        }
        else{
            return ResponseUtil.error("Failed to unblock user.");
        }
    }


    @PostMapping("/complaint")
    public ResponseEntity<?> complaint(@RequestBody ChatComplaintRequest chatComplaintRequest, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ActivityLogDto activityLogDto = new ActivityLogDto();
        activityLogDto.setContent(chatComplaintRequest.getContent());
        activityLogDto.setActivityType(chatComplaintRequest.getActivityType());
        Binding binding = bindingService.findById(chatComplaintRequest.getBindingId());
        if (binding == null) {
            return ResponseUtil.error("Invalid binding ID.");
        }
        User user = userService.findUserById(userPrincipal.getId());

        if (activityLogService.saveActivityLogBinding(activityLogDto, user, binding)) {
            return ResponseUtil.success("Complaint submitted successfully.");
        }
        return ResponseUtil.error("Failed to submit complaint.");    
    }

}
