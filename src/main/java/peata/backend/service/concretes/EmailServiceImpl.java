package peata.backend.service.concretes;

import java.util.List;

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

    
    public void sendToAdmins(List<String> recipients, String publisherEmail, List<String> imageUrls) {
        for (String recipient : recipients) {
            try {
                sendEmailToAdmin(recipient, publisherEmail, imageUrls);
            } catch (MessagingException e) {
                // Log the error and continue with the next recipient
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
    }

    private void sendEmail(String to, String message, String publisherEmail, List<String> imageUrls) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PEATA Bildirisi ");

        // Use Thymeleaf to create the HTML content
        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        String htmlContent = templateEngine.process("emailTemplate", context);
        
        helper.setText(htmlContent, true);
        

        mailSender.send(mimeMessage);
    }

    private void sendEmailToAdmin(String to, String publisherEmail, List<String> imageUrls) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PEATA Bildirisi ");

        // Use Thymeleaf to create the HTML content
        Context context = new Context();
        context.setVariable("message", "Yeni bir ilan geldi");
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        String htmlContent = templateEngine.process("emailTemplate", context);

        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}
