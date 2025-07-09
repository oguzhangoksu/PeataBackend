package peata.backend.service.abstracts;

import peata.backend.entity.Binding;
import peata.backend.entity.ChatMessage;

public interface ChatMessageService {
     public void save(Binding binding, ChatMessage chatMessage);
}
