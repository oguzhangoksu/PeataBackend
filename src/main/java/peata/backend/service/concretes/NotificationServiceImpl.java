package peata.backend.service.concretes;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.core.RabbitMqConfig;

@Service
public class NotificationServiceImpl {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMqConfig rabbitMqConfig;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);


    private static final String EXCHANGE_NAME = "email-exchange";
    private static final String ROUTING_KEY = "email-routing-key";
    private static final String EXCHANGE_REGISTER_NAME = "register-email-exchange";
    private static final String ROUTING_REGISTER_KEY = "register-email-routing-key";
    
    HashMap<String, String> bodyKayipByLanguage = new HashMap<>(){{
        put("tr","Etrafınızda kaybolan bir evcil hayvan bulunmakta. Lütfen gördüğünüz anda verdiğimiz e-mail adresi ile iletişime geçmenizi rica ederiz.");
        put("en","A lost pet has been reported in your area. Kindly contact the issuer of this announcement via the provided email address immediately upon sighting the animal.");
    }};
    HashMap<String, String> bodySahipByLanguage = new HashMap<>(){{
        put("tr","Etrafınızda sahiplenilmek isteyen bir yavrumuz bulunmakta. Eğer iletişime geçmek isterseniz verdiğimiz e-mail adresi ile iletişime geçmenizi rica ederiz.");
        put("en","There is a little one around you looking for a home. If you would like to get in touch, please contact us via the provided email address");
    }};


    public void sendNotification(String publisherEmail, String city, String district, List<String> imageUrls, String addType,String pCode,String language) {
        String message = "";
        String routingKey = city + "." + district; 
    
        logger.info("Preparing to send notification for city: {}, district: {}, with routingKey: {}", city, district, routingKey);


        // Define the message based on addType
        if ("Kayıp".equals(addType)) {
            message =bodyKayipByLanguage.get(language);
        } else {
            message =bodySahipByLanguage.get(language);
        }

        logger.debug("Generated message: {}", message);


        rabbitTemplate.convertAndSend("notifications-exchange", routingKey, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setHeader("publisherEmail", publisherEmail);
            messagePostProcessor.getMessageProperties().setHeader("city", city);
            messagePostProcessor.getMessageProperties().setHeader("district", district);
            messagePostProcessor.getMessageProperties().setHeader("addType", addType);
            messagePostProcessor.getMessageProperties().setHeader("imageUrls", imageUrls);
            messagePostProcessor.getMessageProperties().setHeader("pCode", pCode);
            messagePostProcessor.getMessageProperties().setHeader("language", language);
            return messagePostProcessor;
        });
    
        logger.info("Notification sent to {}: {} with message: {}", city + "/" + district, routingKey, message);
    }

    public void subscribeUserToCityDistrict(String userEmail, String city, String district) {
        String queueName = "queue-" + city + "-" + district; // Dynamic queue name
        String routingKey = city + "." + district; // Routing key for the topic exchange

        logger.info("Subscribing user {} to city: {}, district: {}", userEmail, city, district);


        // Create and declare the queue
        Queue queue = rabbitMqConfig.createDurableQueue(queueName);
        rabbitAdmin.declareQueue(queue); // Declare the queue


        logger.debug("Queue created: {}", queueName);

     
        Binding binding = rabbitMqConfig.createBinding(queue, routingKey); 
        rabbitAdmin.declareBinding(binding); 

        logger.info("Queue {} bound with routingKey: {}", queueName, routingKey);
    }
    
    public void sendCodeVerification(String email, String code, String language){
        logger.info("Sending verification code to email: {}", email);
        String message = "email=" + email + ", code=" + code + ", language=" + language;

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);

        logger.info("Verification code message sent for email: {}", email);

    }

    public void sendRegisterCode(String email, String code, String language){
        logger.info("Sending register code to email: {}", email);
        String message = "email=" + email + ", code=" + code + ", language=" + language;

        rabbitTemplate.convertAndSend(EXCHANGE_REGISTER_NAME, ROUTING_REGISTER_KEY, message);

        logger.info("Verification code message sent for email: {}", email);

    }

}
