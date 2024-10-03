package peata.backend.listeners;
/* 
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.EmailServiceImpl;

@Component
public class NotificationListener {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    private static final int BATCH_SIZE = 100;

    @RabbitListener(queues = "generic-queue")
    public void receiveFromDistrict(String message,
                                     @Header("publisherEmail") String publisherEmail,
                                     @Header("city") String city,
                                     @Header("district") String district,
                                     @Header("imageUrls") List<String> imageUrls) {
        System.out.println("Received message in " + city + "/" + district + ": " + message);
        List<String> userEmails= userService.findEmailsByCityAndDistrict(city, district);
        for (int i = 0; i < userEmails.size(); i += BATCH_SIZE) {
            List<String> batch = userEmails.subList(i, Math.min(userEmails.size(), i + BATCH_SIZE));
            emailServiceImpl.sendBatchEmails(batch, message, publisherEmail, imageUrls);
        }
    }
}
*/