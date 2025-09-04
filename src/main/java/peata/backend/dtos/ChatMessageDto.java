package peata.backend.dtos;

import java.sql.Timestamp;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import peata.backend.entity.ChatMessage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private Long id;
    private Long senderId;
    private String content;
    private Timestamp sentAt;
    private boolean isRead;

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(chatMessage.getId());
        dto.setSenderId(chatMessage.getSenderId());
        dto.setContent(chatMessage.getContent());
        dto.setSentAt(chatMessage.getSentAt());
        dto.setRead(chatMessage.isRead());
        return dto;
    }
}
