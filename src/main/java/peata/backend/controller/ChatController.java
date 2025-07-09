package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import peata.backend.entity.ChatMessage;
import peata.backend.entity.User;
import peata.backend.entity.Binding;
import peata.backend.service.abstracts.BindingService;
import peata.backend.service.abstracts.ChatMessageService;
import peata.backend.service.abstracts.UserService;
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

    
    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send/{bindingId}")
    public void sendMessage(@DestinationVariable Long bindingId, MessageRequest messageRequest, @AuthenticationPrincipal UserPrincipal userPrincipal) { // ChatMessage değişecek
        ChatMessage chatMessage = messageRequest.toChatMessage();
        User user =userService.findUserByUsername(userPrincipal.getUsername());
        Binding binding = bindingService.findByIdOrCreate(bindingId, user.getId(), user.getId());
        chatMessageService.save(binding, chatMessage);

        // Abonelere yayını yap
        messagingTemplate.convertAndSend("/topic/chat/" + bindingId, chatMessage);
    }
}
