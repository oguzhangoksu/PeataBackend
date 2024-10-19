package peata.backend.listeners;

import java.util.List;
import java.util.Map;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.EmailServiceImpl;

@Service
public class DynamicListenerService {

    
    @Autowired
    private ConnectionFactory connectionFactory; // Connection to RabbitMQ



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
            String content = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("Received message in " + city + "/" + district + ": " + message);
            // Call a method to handle the message
            handleMessage(city, district, content, publisherEmail, imageUrls);
        });
        
        container.start(); // Start listening on the queue
    }

    // Method to handle the message after receiving it
    private void handleMessage(String city, String district, String message, String publisherEmail, List<String> imageUrls) {
        System.out.println("Received message in " + city + "/" + district + ": " + message);
        

        // Find emails of users in the same city and district
        List<String> userEmails = userService.findEmailsByCityAndDistrict(city, district, publisherEmail);
        // Send emails in batches
        for (int i = 0; i < userEmails.size(); i += BATCH_SIZE) {
            List<String> batch = userEmails.subList(i, Math.min(userEmails.size(), i + BATCH_SIZE));
            emailServiceImpl.sendBatchEmails(batch, message, publisherEmail, imageUrls);
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

}
