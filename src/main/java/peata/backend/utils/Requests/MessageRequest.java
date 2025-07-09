package peata.backend.utils.Requests;

import lombok.Data;
import peata.backend.entity.ChatMessage;
@Data
public class MessageRequest {

    private Long senderId;
    private String content;

    public ChatMessage toChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(this.senderId);
        chatMessage.setContent(this.content);
        return chatMessage;
    }
}
