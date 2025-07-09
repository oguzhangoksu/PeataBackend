package peata.backend.dtos;
import lombok.Data;
@Data
public class ChatMessageDto {

    private Long senderId;
    private String content;
}
