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

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.EmailServiceImpl;
import peata.backend.service.concretes.NotificationServiceImpl;
import peata.backend.utils.CustomMessageConverter;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;


@Service
public class DynamicListenerService {

    
    @Autowired
    private ConnectionFactory connectionFactory; // Connection to RabbitMQ

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final UserService userService;

    @Autowired
    public DynamicListenerService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private EmailServiceImpl emailServiceImpl; // To send batch emails

    private final int BATCH_SIZE = 100; // Batch size for sending emails

    public void createListener(String city, String district) {
        String queueName = "queue-" + city + "-" + district;
        // Step 2: Create a listener container for each dynamic queue
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory); // Connect to RabbitMQ
        container.setQueueNames(queueName); // Specify the queue to listen to

        // Define what happens when a message is received
        container.setMessageListener(message -> {
            String publisherEmail = message.getMessageProperties().getHeader("publisherEmail");
            List<String> imageUrls = message.getMessageProperties().getHeader("imageUrls");
            String pCode = message.getMessageProperties().getHeader("pCode");
            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received message in " + city + "/" + district + ": " + message);
            // Call a method to handle the message
            handleMessage(city, district, content, publisherEmail, imageUrls, pCode);
        });
        
        container.start(); // Start listening on the queue
    }

    // Method to handle the message after receiving it
    private void handleMessage(String city, String district, String message, String publisherEmail, List<String> imageUrls ,String pCode) {
        System.out.println("Received message in " + city + "/" + district + ": " + message);
        

        // Find emails of users in the same city and district
        List<String> userEmails = userService.findEmailsByCityAndDistrict(city, district, publisherEmail);
        // Send emails in batches
        for (int i = 0; i < userEmails.size(); i += BATCH_SIZE) {
            List<String> batch = userEmails.subList(i, Math.min(userEmails.size(), i + BATCH_SIZE));
            emailServiceImpl.sendBatchEmails(batch, message, publisherEmail, imageUrls,pCode);
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
