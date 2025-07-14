package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import peata.backend.entity.User;
import peata.backend.dtos.BindingDto;
import peata.backend.entity.Binding;
import peata.backend.service.abstracts.BindingService;
import peata.backend.service.abstracts.ChatMessageService;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;

@RestController()
@RequestMapping("api")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

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


    
 
}
