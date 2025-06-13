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
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import peata.backend.core.RabbitMqConfig;
import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.EmailServiceImpl;
import peata.backend.service.concretes.NotificationServiceImpl;
import peata.backend.utils.CustomMessageConverter;
import com.google.firebase.messaging.Notification;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import com.google.firebase.messaging.Message;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

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
    private final RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitMqConfig rabbitMqConfig;

    @Autowired
    public DynamicListenerService(@Lazy UserService userService,RabbitAdmin rabbitAdmin) {
        this.userService = userService;
        this.rabbitAdmin = rabbitAdmin;
    }

    @Autowired
    private EmailServiceImpl emailServiceImpl; 

    private final int BATCH_SIZE = 100; 

    public void createListener(String city, String district) {
        String queueName = "queue-" + city + "-" + district;
        declareQueueIfNotExist(queueName, city + "." + district);
        
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName); 

        container.setMessageListener(message -> {
            String publisherEmail = message.getMessageProperties().getHeader("publisherEmail");
            List<String> imageUrls = message.getMessageProperties().getHeader("imageUrls");
            String addType = message.getMessageProperties().getHeader("addType");
            String pCode = message.getMessageProperties().getHeader("pCode");
            String language = message.getMessageProperties().getHeader("language");
            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received message in " + city + "/" + district + ": " + message);
            // Call a method to handle the message
            handleMessage(city, district, content, publisherEmail, imageUrls, pCode,addType,language);
        });
        
        container.start(); // Start listening on the queue
    }
    private void declareQueueIfNotExist(String queueName, String routingKey) {
        try {
            logger.info("Checking if the queue {} exists.", queueName);
    
            if (rabbitAdmin.getQueueProperties(queueName) == null) {
                logger.info("Queue {} does not exist. Declaring a new queue.", queueName);
                Queue queue = rabbitMqConfig.createDurableQueue(queueName);
                rabbitAdmin.declareQueue(queue);
                Binding binding = rabbitMqConfig.createBinding(queue, routingKey); 
                rabbitAdmin.declareBinding(binding);
    
                logger.info("Queue declared and binding created for queue: {} with routingKey: {}", queueName, routingKey);
            } else {
                logger.info("Queue {} already exists.", queueName);
            }
        } catch (Exception e) {
            logger.error("Error while declaring queue {}: {}", queueName, e.getMessage(), e);
        }
    }

    private void handleMessage(String city, String district, String message, String publisherEmail, List<String> imageUrls ,String pCode,String addType,String language) {
        logger.info("Received message in " + city + "/" + district + ": " + message);
        

        List<String> userEmails = userService.findEmailsByCityAndDistrictOnValidateEmail(city, district, publisherEmail,language);
        List<String> userDeviceTokens = userService.getAllUsersDeviceToken(city, district, publisherEmail,language);

        for (int i = 0; i < userEmails.size(); i += BATCH_SIZE) {
            List<String> batch = userEmails.subList(i, Math.min(userEmails.size(), i + BATCH_SIZE));
            emailServiceImpl.sendBatchEmails(batch, message, publisherEmail, imageUrls,pCode,language);
        }
        
        sendNotificationsToDevices(userDeviceTokens, message, publisherEmail,pCode,addType,language);
    }

    //Firebase Notification system
    private void sendNotificationsToDevices(List<String> deviceTokens, String messageContent, String publisherEmail, String pCode,String addType,String language) {
        HashMap<String, String> titleByLanguage = new HashMap<>(){{
            put("tr", "Evcil Hayvana Yardım Edin 🔔");
            put("en", "Help A Pet Today 🔔");
        }};
        HashMap<String, String> bodyKayipByLanguage = new HashMap<>(){{
            put("tr", "🐾 Kayıp evcil hayvan ilanı çevrenizde bulundu. İlanı görmek için tıklayın.");
            put("en", "🐾 There is a lost pet in your area.");
        }};
        HashMap<String, String> bodySahipByLanguage = new HashMap<>(){{
            put("tr", "🏡 Sahiplendirme ilanı çevrenizde açıldı. İlanı görmek için tıklayın.");
            put("en", "🏡 There is a pet in your area waiting to be adopted.");
        }};

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
                notification.put("title", titleByLanguage.get(language));
                notification.put("body", bodyKayipByLanguage.get(language));
            } else {
                notification.put("title", titleByLanguage.get(language));
                notification.put("body", bodySahipByLanguage.get(language));
            }
            Map<String, Object>  apns = new HashMap<>();
            Map<String, Object>  payload = new HashMap<>();
            Map<String, Object>  aps = new HashMap<>();
            aps.put("sound","default");
            payload.put("aps",aps);
            apns.put("payload",payload);
            Map<String, String> data = new HashMap<>();
            data.put("pCode", pCode); 
            
            Map<String, Object> message = new HashMap<>();
            logger.info("deviceTokens:{}", deviceTokens);
            
            for (String token : deviceTokens) {
                try {
                    message.put("token", token);
                    message.put("notification", notification);
                    message.put("data", data); 
                    message.put("apns",apns);
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
    public void receiveMessage(String messageBody) {
        logger.info("Received message from email-queue: {}", messageBody);
        Map<String, String> message = parseMessage(messageBody);
        String email = message.get("email");
        String code = message.get("code");
        String language = message.get("language");

        if (email == null || code == null) {
            logger.warn("Message missing required fields: email={}, code={}", email, code);
            return;
        }

        try {
            emailServiceImpl.sendVerificationCode(email, code,language);
            logger.info("Verification code successfully sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email to " + email, e);
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
                    String language = messageData.get("language");

                    // Eğer eksik değer varsa logla ve işlemi sonlandır
                    if (email == null || code == null) {
                        logger.warn("Message missing required fields: email={} code={} language={}", email, code, language);
                        return;
                    }

                    // Email gönder
                    emailServiceImpl.sendRegisterCode(email, code, language);
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
                    String language = messageData.get("language");
                    if (email == null || code == null) {
                        logger.warn("Message missing required fields: email={} code={}", email, code);
                        return;
                    }
                    emailServiceImpl.sendVerificationCode(email, code, language);  
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
