package peata.backend.service.concretes;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final List<String> admins =Arrays.asList("yuky.yt@gmail.com", "oguzhang15@hotmail.com");
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;


    HashMap<String, String> languageTemplate = new HashMap<>(){{
        put("tr","emailTemplate2");
        put("en","emailTemplateEn");
    }};
    HashMap<String, String> languageSubject = new HashMap<>(){{
        put("tr","PatyApp Bildirisi");
        put("en","PatyApp Notification");
    }};

    HashMap<String, String> subjectRegisterByLanguage = new HashMap<>(){{
        put("tr","Kayıt Onaylama Kodu");
        put("en","Registration Confirmation Code");
    }};
    HashMap<String, String> bodyRegisterByLanguage = new HashMap<>(){{
        put("tr","Kayıt Onaylama Kodu");
        put("en","Registration Confirmation Code");
    }};
    HashMap<String, String> tempRegisterByLanguage = new HashMap<>(){{
        put("tr","registerVerificationTemplateTr");
        put("en","registerVerificationTemplateEn");
    }};
    HashMap<String, String> tempPasswordChangeByLanguage = new HashMap<>(){{
        put("tr","verificationCodeTemplateTr");
        put("en","verificationCodeTemplateEn");
    }};
    HashMap<String, String> subjectPasswordChangeByLanguage = new HashMap<>(){{
        put("tr","Şifre Yenileme Kodu");
        put("en","Password Reset Code");
    }};
    HashMap<String, String> bodyPasswordChangeByLanguage = new HashMap<>(){{
        put("tr","Şifre yenileme Kodunuz:");
        put("en","Your Password Reset Code:");
    }};


    public void sendBatchEmails(List<String> recipients, String message, String publisherEmail, List<String> imageUrls, String pCode, String language) {
        logger.info("Starting batch email sending process to {} recipients.", recipients.size());

        for (String recipient : recipients) {
            try {
                sendEmail(recipient, message, publisherEmail, imageUrls , pCode, language);
                logger.info("Email successfully sent to {}", recipient);
            } catch (MessagingException e) {

                logger.error("Failed to send email to {}: {}", recipient, e.getMessage());

            }
        }
        logger.info("Batch email sending process completed.");
    }

    
    public void sendToAdmins( String publisherEmail, List<String> imageUrls,Long addId ,String pCode,String language) {
        logger.info("Sending email to admins regarding Add ID: {}", addId);

        for(String image: imageUrls){
            logger.debug("Image URL: {}", image);
        }
        for (String admin : admins) {
            try {
                sendEmailToAdmin(admin, publisherEmail, imageUrls, addId,pCode,language);
                logger.info("Email successfully sent to admin: {}", admin);
            } catch (MessagingException e) {
                logger.error("Failed to send email to admin {}: {}", admin, e.getMessage());
            }
        }

        logger.info("Admin email sending process completed.");

    }

    private void sendEmail(String to, String message, String publisherEmail, List<String> imageUrls, String pCode,String language) throws MessagingException{
        logger.debug("Preparing email to be sent to: {}", to);
       

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        try {
            helper.setFrom(new InternetAddress("info@patyapp.com.tr", "Paty Noreply"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to set sender display name: {}", e.getMessage());
        }
       
        helper.setTo(to);
        helper.setSubject(languageSubject.get(language));

        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        context.setVariable("pCode", pCode);
        String htmlContent = templateEngine.process(languageTemplate.get(language), context);
        
        helper.setText(htmlContent, true);
        mimeMessage.setHeader("Importance", "High");
        mimeMessage.setHeader("X-Priority", "1"); 
        mimeMessage.setHeader("Priority", "urgent");

        logger.debug("Email to {} prepared successfully.", to);
        mailSender.send(mimeMessage);
       

    }

    private void sendEmailToAdmin(String to, String publisherEmail, List<String> imageUrls, Long addId,String pCode,String language) throws MessagingException {
        logger.debug("Preparing email to admin: {}", to);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(languageSubject.get(language));

        Context context = new Context();
        context.setVariable("message", "Yeni bir ilan geldi. Ad ID'si:"+addId);
        context.setVariable("publisherEmail", publisherEmail);
        context.setVariable("imageUrls", imageUrls);
        context.setVariable("pCode", pCode);
        String htmlContent = templateEngine.process(languageTemplate.get(language), context);

        helper.setText(htmlContent, true);
        mimeMessage.setHeader("Importance", "High");
        mimeMessage.setHeader("X-Priority", "1");
        mimeMessage.setHeader("Priority", "urgent");

        logger.debug("Email to admin {} prepared successfully.", to);
        mailSender.send(mimeMessage);
    }

    public void sendVerificationCode(String to, String code, String language) throws MessagingException{
        logger.info("Sending verification code email to {}", to);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subjectPasswordChangeByLanguage.get(language));
        Context context = new Context();
        context.setVariable("message", bodyPasswordChangeByLanguage.get(language));
        context.setVariable("code",code);
        String htmlContent = templateEngine.process(tempPasswordChangeByLanguage.get(language), context);

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        
        logger.info("Verification code email successfully sent to {}", to);
        
    }
    //HTML Halledilecek
    
    public void sendRegisterCode(String to , String code , String language) throws MessagingException{
        logger.info("Sending register code email to {}", to);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subjectRegisterByLanguage.get(language));
        Context context = new Context();
        context.setVariable("message", bodyRegisterByLanguage.get(language));
        context.setVariable("code",code);
        String htmlContent = templateEngine.process(tempRegisterByLanguage.get(language), context);

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        
        logger.info("Register code email successfully sent to {}", to);
    }
}
