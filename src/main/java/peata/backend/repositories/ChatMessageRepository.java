package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    
}
