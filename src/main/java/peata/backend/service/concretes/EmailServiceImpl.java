package peata.backend.service.concretes;

import java.util.List;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final List<String> admins =Arrays.asList("yusufturhag@outlook.com", "oguzhang15@hotmail.com");
      @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendBatchEmails(List<String> recipients, String message, String publisherEmail, List<String> imageUrls, String pCode) {
        logger.info("Starting batch email sending process to {} recipients.", recipients.size());

        for (String recipient : recipients) {
            try {
                sendEmail(recipient, message, publisherEmail, imageUrls , pCode);
                logger.info("Email successfully sent to {}", recipient);
            } catch (MessagingException e) {

                logger.error("Failed to send email to {}: {}", recipient, e.getMessage());

            }
        }
        logger.info("Batch email sending process completed.");
    }

    
    public void sendToAdmins( String publisherEmail, List<String> imageUrls,Long addId) {
        logger.info("Sending email to admins regarding Add ID: {}", addId);

        for(String image: imageUrls){
            logger.debug("Image URL: {}", image);
        }
        for (String admin : admins) {
            try {
                sendEmailToAdmin(admin, publisherEmail, imageUrls, addId);
                logger.info("Email successfully sent to admin: {}", admin);
            } catch (MessagingException e) {
                logger.error("Failed to send email to admin {}: {}", admin, e.getMessage());
            }
        }

        logger.info("Admin email sending process completed.");

    }

    private void sendEmail(String to, String message, String publisherEmail, List<String> imageUrls, String pCode) throws MessagingException {
        logger.debug("Preparing email to be sent to: {}", to);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("PEATA Bildirisi ");

        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        context.setVariable("addId", pCode);
        String htmlContent = templateEngine.process("emailTemplate", context);
        
        helper.setText(htmlContent, true);
        mimeMessage.setHeader("Importance", "High");
        mimeMessage.setHeader("X-Priority", "1"); 
        mimeMessage.setHeader("Priority", "urgent");

        logger.debug("Email to {} prepared successfully.", to);
        mailSender.send(mimeMessage);
    }

    private void sendEmailToAdmin(String to, String publisherEmail, List<String> imageUrls, Long addId) throws MessagingException {
        logger.debug("Preparing email to admin: {}", to);
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

        logger.debug("Email to admin {} prepared successfully.", to);
        mailSender.send(mimeMessage);
    }

    public void sendVerificationCode(String to, String code) throws MessagingException{
        logger.info("Sending verification code email to {}", to);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Şifre Yenileme Kodu");
        Context context = new Context();
        context.setVariable("message", "Şifre yenileme Kodunuz:");
        context.setVariable("code",code);
        String htmlContent = templateEngine.process("verificationCodeTemplate", context);

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        
        logger.info("Verification code email successfully sent to {}", to);
        
    }
}
