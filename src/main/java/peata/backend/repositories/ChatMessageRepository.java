package peata.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.ChatMessage;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByBindingId(Long bindingId);
    
  
}
