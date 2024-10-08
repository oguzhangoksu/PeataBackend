package peata.backend.service.concretes;

import java.util.List;

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

    // Method to send message to a specific city and district
    public void sendNotification(String publisherEmail, String city, String district, List<String> imageUrls, String addType) {
        String message = "";
        String routingKey = city + "." + district; // Use dynamic routing key
    
        // Define the message based on addType
        if ("Kayıp".equals(addType)) {
            message = "Etrafınızda kaybolan bir evcil hayvan bulunmakta. Lütfen gördüğünüz anda verdiğimiz e-mail adresi ile iletişime geçmenizi rica ederiz.";
        } else {
            message = "Etrafınızda sahiplenilmek isteyen bir yavrumuz bulunmakta. Eğer iletişime geçmek isterseniz verdiğimiz e-mail adresi ile iletişime geçmenizi rica ederiz.";
        }
        // Send the message to the correct exchange with routing key
        rabbitTemplate.convertAndSend("notifications-exchange", routingKey, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setHeader("publisherEmail", publisherEmail);
            messagePostProcessor.getMessageProperties().setHeader("city", city);
            messagePostProcessor.getMessageProperties().setHeader("district", district);
            messagePostProcessor.getMessageProperties().setHeader("imageUrls", imageUrls);
            return messagePostProcessor;
        });
    
        System.out.println("Sent notification to " + city + "/" + district + ": " + message);
    }

        public void subscribeUserToCityDistrict(String userEmail, String city, String district) {
            String queueName = "queue-" + city + "-" + district; // Dynamic queue name
            String routingKey = city + "." + district; // Routing key for the topic exchange

            // Create and declare the queue
            Queue queue = rabbitMqConfig.createDurableQueue(queueName);
            rabbitAdmin.declareQueue(queue); // Declare the queue

            // Create and declare the binding with the correct routing key
            Binding binding = rabbitMqConfig.createBinding(queue, routingKey); 
            rabbitAdmin.declareBinding(binding); // Declare the binding in RabbitMQ
            System.out.println("Queue created and bound: " + queueName + " with routing key: " + routingKey);
    }

}
