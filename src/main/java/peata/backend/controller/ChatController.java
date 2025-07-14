package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import peata.backend.entity.ChatMessage;
import peata.backend.entity.User;
import peata.backend.dtos.BindingDto;
import peata.backend.entity.Binding;
import peata.backend.service.abstracts.BindingService;
import peata.backend.service.abstracts.ChatMessageService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.MessageRequest;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private BindingService bindingService;

    @GetMapping("/api/chat/initiate/{addId}")
    public ResponseEntity<?> initiateChat(@PathVariable Long addId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User sender = userService.findUserByUsername(userPrincipal.getUsername());
        Binding binding = bindingService.findByIdOrCreate(addId, sender.getId());
        BindingDto bindingDto = new BindingDto().convertFromEntity(binding);
        return ResponseUtil.success("Chat initiated successfully.", bindingDto);
    }

    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }
    
    @MessageMapping("/send/{bindingId}")
    public void sendMessage(@DestinationVariable Long bindingId, MessageRequest messageRequest, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        System.out.println("=== ChatController.sendMessage called ===");
        System.out.println("BindingId: " + bindingId);
        System.out.println("MessageRequest: " + messageRequest);
        System.out.println("UserPrincipal: " + userPrincipal);
        
        // Null kontrolü ekle
        if (userPrincipal == null) {
            System.out.println("ERROR: UserPrincipal is null - authentication failed");
            throw new RuntimeException("Authentication required - UserPrincipal is null");
        }
        
        System.out.println("Username from UserPrincipal: " + userPrincipal.getUsername());
        
        if (userPrincipal.getUsername() == null) {
            System.out.println("ERROR: Username is null in UserPrincipal");
            throw new RuntimeException("Username is null in UserPrincipal");
        }
        
        // AuthenticationPrincipal'dan username'i al
        User sender = userService.findUserByUsername(userPrincipal.getUsername());
        ChatMessage chatMessage = messageRequest.toChatMessage(sender.getId());
        Binding binding = bindingService.findById(bindingId); 
        chatMessageService.save(binding, chatMessage);
        
        // Abonelere yayını yap
        messagingTemplate.convertAndSend("/topic/chat/" + bindingId, chatMessage);
        
        System.out.println("Message sent successfully");
    }
}
