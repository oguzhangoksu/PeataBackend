package peata.backend.service.concretes;

import java.util.List;
import java.util.Arrays;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailServiceImpl {
    private final List<String> admins =Arrays.asList("yusufturhag@outlook.com", "oguzhang15@hotmail.com");
      @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendBatchEmails(List<String> recipients, String message, String publisherEmail, List<String> imageUrls) {
        for (String recipient : recipients) {
            try {
                sendEmail(recipient, message, publisherEmail, imageUrls);
            } catch (MessagingException e) {
                // Log the error and continue with the next recipient
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
    }

    
    public void sendToAdmins( String publisherEmail, List<String> imageUrls,Long addId) {
        for(String image: imageUrls){
            System.out.println("image:"+image);
        }
        for (String admin : admins) {
            try {
                sendEmailToAdmin(admin, publisherEmail, imageUrls, addId);
            } catch (MessagingException e) {
                // Log the error and continue with the next recipient
                System.err.println("Failed to send email to " + admin + ": " + e.getMessage());
            }
        }
    }

    private void sendEmail(String to, String message, String publisherEmail, List<String> imageUrls) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PEATA Bildirisi ");

        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        String htmlContent = templateEngine.process("emailTemplate", context);
        
        helper.setText(htmlContent, true);
        mimeMessage.setHeader("Importance", "High");
        mimeMessage.setHeader("X-Priority", "1"); 
        mimeMessage.setHeader("Priority", "urgent");

        mailSender.send(mimeMessage);
    }

    private void sendEmailToAdmin(String to, String publisherEmail, List<String> imageUrls, Long addId) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PEATA Bildirisi ");

        Context context = new Context();
        context.setVariable("message", "Yeni bir ilan geldi. Ad ID'si:"+addId);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        String htmlContent = templateEngine.process("emailTemplate", context);

        helper.setText(htmlContent, true);
        mimeMessage.setHeader("Importance", "High");
        mimeMessage.setHeader("X-Priority", "1");
        mimeMessage.setHeader("Priority", "urgent");

        mailSender.send(mimeMessage);
    }
}
