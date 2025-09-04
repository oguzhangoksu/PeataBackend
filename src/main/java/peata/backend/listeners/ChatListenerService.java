package peata.backend.listeners;

import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import peata.backend.dtos.ChatMessageCreatedPayload;
import peata.backend.entity.User;
import peata.backend.entity.UserDevice;
import peata.backend.repositories.UserDeviceRepository;
import peata.backend.service.abstracts.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatListenerService {

    private static final Logger logger = LoggerFactory.getLogger(ChatListenerService.class);
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/paty-a11a3/messages:send";

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleCredentials googleCredentials;

    @RabbitListener(
        queues = "chat.push.q",
        ackMode = "MANUAL",
        containerFactory = "chatRabbitListenerContainerFactory"
    )
    public void handle(ChatMessageCreatedPayload payload, Channel ch, Message amqpMsg) throws Exception {
        long tag = amqpMsg.getMessageProperties().getDeliveryTag();
        
        try {
            logger.info("Chat message created event received: messageId={}, bindingId={}, recipientId={}", 
                       payload.getMessageId(), payload.getBindingId(), payload.getRecipientId());

            // recipientId'ye göre kullanıcıyı bul
            User recipient = userService.findUserById(payload.getRecipientId());
            if (recipient == null) {
                logger.warn("Recipient user not found with ID: {}", payload.getRecipientId());
                ch.basicAck(tag, false);
                return;
            }

            // Kullanıcının device token'larını al
            List<UserDevice> userDevices = userDeviceRepository.findByUserId(payload.getRecipientId());
            UserDevice lastUserDevice = userDevices.get(userDevices.size() - 1); // Son elemanı al
            if (lastUserDevice == null) {
                logger.info("No device tokens found for recipient user: {}", recipient.getUsername());
                ch.basicAck(tag, false);
                return;
            }
            
            sendChatNotification(lastUserDevice.getDeviceToken(), recipient.getLanguage());
        

            ch.basicAck(tag, false);

        } catch (Exception e) {
            logger.error("Error processing chat message created event: {}", e.getMessage(), e);
            ch.basicNack(tag, false, true); // Retry mekanizması için nack
        }
    }

    private void sendChatNotification(String deviceToken, String language) {
        try {
            // Dil bazlı mesajlar
            Map<String, String> titleByLanguage = Map.of(
                "tr", "Yeni Mesaj 💬",
                "en", "New Message 💬"
            );

            Map<String, String> bodyByLanguage = Map.of(
                "tr", "Size yeni bir mesaj geldi",
                "en", "You have a new message"
            );

            // Firebase credentials'ı yenile
            googleCredentials.refreshIfExpired();
            AccessToken accessToken = googleCredentials.getAccessToken();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken.getTokenValue());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Bildirim içeriği
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", titleByLanguage.getOrDefault(language, titleByLanguage.get("tr")));
            notification.put("body", bodyByLanguage.getOrDefault(language, bodyByLanguage.get("tr")));

            // iOS için ses ayarı
            Map<String, Object> apns = new HashMap<>();
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> aps = new HashMap<>();
            aps.put("sound", "default");
            payload.put("aps", aps);
            apns.put("payload", payload);

            // Data payload (chat sayfasına yönlendirme için)
            Map<String, String> data = new HashMap<>();
            data.put("type", "chat_message");
            data.put("action", "open_chat");

            // Ana mesaj objesi
            Map<String, Object> message = new HashMap<>();
            message.put("token", deviceToken);
            message.put("notification", notification);
            message.put("data", data);
            message.put("apns", apns);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(FCM_API_URL, request, String.class);

            logger.info("Chat notification sent successfully to device token: {} - Response: {}", 
                       deviceToken, response.getBody());

        } catch (Exception e) {
            logger.error("Failed to send chat notification to device token {}: {}", deviceToken, e.getMessage(), e);
        }
    }
}