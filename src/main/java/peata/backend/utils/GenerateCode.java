package peata.backend.utils;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class GenerateCode {

    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // Generates a number between 100000 and 999999
        return String.valueOf(code);
    }

    
    

}
