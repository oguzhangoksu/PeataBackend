package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Binding;
import peata.backend.entity.ChatMessage;
import peata.backend.repositories.ChatMessageRepository;
import peata.backend.service.abstracts.ChatMessageService;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    

    @Override
    public void save(Binding binding, ChatMessage chatMessage) {
    
        chatMessage.setBinding(binding);
        chatMessage.setRead(false); 
        chatMessageRepository.save(chatMessage);
    }
}
