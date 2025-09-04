package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.entity.Binding;
import peata.backend.entity.ChatMessage;
import peata.backend.entity.GeneralRule;
import peata.backend.entity.User;

public interface ChatMessageService {
     public void save(Binding binding, Long senderId,String content);
     public List<ChatMessage> getMessagesByBindingId(Long bindingId);
     public GeneralRule getActiveGeneralRule(String version);
     public String checkBlockStatus(User sender, Binding binding);
}
