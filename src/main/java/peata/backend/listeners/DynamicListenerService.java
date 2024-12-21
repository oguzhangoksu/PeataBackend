package peata.backend.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.AllowedListDeserializingMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.EmailServiceImpl;
import peata.backend.service.concretes.NotificationServiceImpl;
import peata.backend.utils.CustomMessageConverter;
import com.google.firebase.messaging.Notification;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import com.google.firebase.messaging.Message;

import org.springframework.http.*;


@Service
public class DynamicListenerService {

    
    @Autowired
    private ConnectionFactory connectionFactory; // Connection to RabbitMQ

    @Autowired
    private GoogleCredentials googleCredentials;

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/paty-a11a3/messages:send";
    private final UserService userService;

    @Autowired
    public DynamicListenerService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private EmailServiceImpl emailServiceImpl; 

    private final int BATCH_SIZE = 100; 

    public void createListener(String city, String district) {
        String queueName = "queue-" + city + "-" + district;
 
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName); 

        container.setMessageListener(message -> {
            String publisherEmail = message.getMessageProperties().getHeader("publisherEmail");
            List<String> imageUrls = message.getMessageProperties().getHeader("imageUrls");
            String addType = message.getMessageProperties().getHeader("addType");
            String pCode = message.getMessageProperties().getHeader("pCode");
            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received message in " + city + "/" + district + ": " + message);
            // Call a method to handle the message
            handleMessage(city, district, content, publisherEmail, imageUrls, pCode,addType);
        });
        
        container.start(); // Start listening on the queue
    }

    private void handleMessage(String city, String district, String message, String publisherEmail, List<String> imageUrls ,String pCode,String addType) {
        System.out.println("Received message in " + city + "/" + district + ": " + message);
        

        List<String> userEmails = userService.findEmailsByCityAndDistrictOnValidateEmail(city, district, publisherEmail);
        List<String> userDeviceTokens = userService.getAllUsersDeviceToken(city, district, publisherEmail);

        for (int i = 0; i < userEmails.size(); i += BATCH_SIZE) {
            List<String> batch = userEmails.subList(i, Math.min(userEmails.size(), i + BATCH_SIZE));
            emailServiceImpl.sendBatchEmails(batch, message, publisherEmail, imageUrls,pCode);
        }
        
        sendNotificationsToDevices(userDeviceTokens, message, publisherEmail,pCode,addType);
    }

    //Firebase Notification system
    private void sendNotificationsToDevices(List<String> deviceTokens, String messageContent, String publisherEmail, String pCode,String addType) {
        try {
            googleCredentials.refreshIfExpired();
            AccessToken accessToken = googleCredentials.getAccessToken();
            logger.info("Firebase credentials refreshed: {}", accessToken.getTokenValue());
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken.getTokenValue());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> notification = new HashMap<>();
            if ("Kayıp".equals(addType)) {
                notification.put("title", "Bulunduğunuz İlçede Bir İlan Açıldı");
                notification.put("body", "Kayıp evcil hayvan ilanı çevrenizde bulundu. İlanı görmek için tıklayın.");
            } else {
                notification.put("title", "Bulunduğunuz İlçede Bir İlan Açıldı");
                notification.put("body", "Sahiplendirme ilanı çevrenizde açıldı. İlanı görmek için tıklayın.");
            }

            Map<String, String> data = new HashMap<>();
            data.put("pCode", pCode); 
            
            Map<String, Object> message = new HashMap<>();
            logger.info("deviceTokens:{}", deviceTokens);
            
            for (String token : deviceTokens) {
                try {
                    message.put("token", token);
                    message.put("notification", notification);
                    message.put("data", data); 
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("message", message);
                    
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                    ResponseEntity<String> response = restTemplate.postForEntity(FCM_API_URL, request, String.class);
                    
                    logger.info("Bildirim cihaz tokenına gönderildi: {}", response.getBody());
                } catch (Exception e) {
                    logger.error("Bildirim gönderirken hata oluştu: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Firebase işlemleri sırasında hata oluştu: {}", e.getMessage());
        }
        
        
    }


    @RabbitListener(queues = "email-queue")
    public void receiveMessage(Map<String, String> message) {
        String email = message.get("email");
        String code = message.get("code");

        try {
            emailServiceImpl.sendVerificationCode(email, code);
            System.out.println("Verification code sent to " + email);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email to " + email);
        }
    }
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("email-queue", "register-email-queue");
        container.setMessageListener(createMessageListener());
        return container;
    }
    
   private MessageListener createMessageListener() {
        return message -> {
            String queueName = message.getMessageProperties().getConsumerQueue();
            if(queueName =="register-email-queue"){
                try {
                    logger.info("Received message: {}", new String(message.getBody()));

                    // Mesajı ayrıştır ve email ve code değerlerini al
                    Map<String, String> messageData = parseMessage(new String(message.getBody()));

                    String email = messageData.get("email");
                    String code = messageData.get("code");

                    // Eğer eksik değer varsa logla ve işlemi sonlandır
                    if (email == null || code == null) {
                        logger.warn("Message missing required fields: email={} code={}", email, code);
                        return;
                    }

                    // Email gönder
                    emailServiceImpl.sendRegisterCode(email, code);
                    logger.info("Sent verification email to: {}", email);

                } catch (Exception e) {
                    logger.error("Error processing message: {}", e.getMessage(), e);
                }
            }
            else if (queueName == "email-queue") {
                try {
                    logger.info("Received message: {}", new String(message.getBody()));
                    
                    Map<String, String> messageData = parseMessage(new String(message.getBody()));
                    String email = messageData.get("email");
                    String code = messageData.get("code");
                    if (email == null || code == null) {
                        logger.warn("Message missing required fields: email={} code={}", email, code);
                        return;
                    }
                    emailServiceImpl.sendVerificationCode(email, code);
                    System.out.println("Verification code sent to " + email);
                }
                catch(Exception e){
                    logger.error("Error processing message: {}", e.getMessage(), e);
                }
                    
            }
        };
    }



    private Map<String, String> parseMessage(String messageBody) {
        Map<String, String> messageData = new HashMap<>();

        String[] pairs = messageBody.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                messageData.put(keyValue[0], keyValue[1]);
            } else {
                logger.warn("Invalid key-value pair in message: {}", pair);
            }
        }
        return messageData;
    }

}
