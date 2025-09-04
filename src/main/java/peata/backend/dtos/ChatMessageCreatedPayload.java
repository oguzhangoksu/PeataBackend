package peata.backend.dtos;


import lombok.Data;

@Data
public class ChatMessageCreatedPayload {
    private Long messageId;
    private Long bindingId;
    private Long recipientId;
}
