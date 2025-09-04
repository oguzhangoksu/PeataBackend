package peata.backend.service.concretes;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Binding;
import peata.backend.entity.ChatMessage;
import peata.backend.entity.GeneralRule;
import peata.backend.entity.User;
import peata.backend.repositories.ChatMessageRepository;
import peata.backend.repositories.GeneralRuleRepository;
import peata.backend.service.abstracts.BindingService;
import peata.backend.service.abstracts.ChatMessageService;
import peata.backend.service.abstracts.UserService;
import peata.backend.dtos.ChatMessageCreatedPayload;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private GeneralRuleRepository generalRuleRepository;
    
    @Autowired
    private ChatEventPublisher chatEventPublisher;

    @Autowired
    private UserBindingSettingsServiceImpl userBindingSettingsService;

    @Autowired
    private UserService userService;

    ChatMessageServiceImpl(GeneralRuleRepository generalRuleRepository) {
        this.generalRuleRepository = generalRuleRepository;
    }
    
    //  @Autowired
    // private BindingService bindingService;

    public void save(Binding binding, Long senderId,String content) {
        User sender = userService.findUserById(senderId);
        if(userBindingSettingsService.isMuted(binding, sender)){
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setContent(content);
        chatMessage.setBinding(binding);
        chatMessage.setRead(false); 
        ChatMessage dbMessage = chatMessageRepository.save(chatMessage);
        ChatMessageCreatedPayload payload = new ChatMessageCreatedPayload();
        payload.setMessageId(dbMessage.getId());
        payload.setBindingId(binding.getId());
        payload.setRecipientId(binding.getRequesterId() == senderId ? binding.getOwnerId() : binding.getRequesterId());
        chatEventPublisher.publishMessageCreated(payload);
    }

    public List<ChatMessage> getMessagesByBindingId(Long bindingId) {

        List<ChatMessage> chatMessages = chatMessageRepository.findByBindingId(bindingId);

        if (chatMessages.isEmpty()) {
            return List.of(); 
        }
        chatMessages.forEach(chatMessage -> {
            chatMessage.setRead(true); 
            chatMessageRepository.save(chatMessage);
        });
        
        return chatMessages;
    }


    public GeneralRule getActiveGeneralRule(String version) {
        return generalRuleRepository.findByVersion_VersionAndIsActiveTrue(version).orElse(null);
    }

    public String checkBlockStatus(User sender, Binding binding) {
        boolean senderIsOwner = sender.getId().equals(binding.getOwnerId());
        Long otherUserId = senderIsOwner ? binding.getRequesterId() : binding.getOwnerId();
        
        boolean isBlocked = sender.getBlockedUsers().stream()
            .anyMatch(user -> user.getId().equals(otherUserId));
        
        return isBlocked ? "You have blocked this user." : null;
    }


}
