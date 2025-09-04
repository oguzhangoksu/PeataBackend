package peata.backend.service.concretes;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import peata.backend.core.RabbitMqConfig;
import peata.backend.dtos.ChatMessageCreatedPayload;




@Service
public class ChatEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public ChatEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void publishMessageCreated(ChatMessageCreatedPayload payload) {
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.CHAT_EXCHANGE,
            "chat.message.created",
            payload,
            m -> { m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); return m; }
        );
    }

}
