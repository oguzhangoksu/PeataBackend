package peata.backend.utils.Requests;

import lombok.Data;
import peata.backend.entity.ChatMessage;
@Data
public class MessageRequest {
    private Long addId;
    private String content;

    public ChatMessage toChatMessage(Long senderId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setContent(this.content);
        return chatMessage;
    }
}
