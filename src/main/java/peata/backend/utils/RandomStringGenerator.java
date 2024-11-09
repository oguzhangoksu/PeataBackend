package peata.backend.utils;
import java.util.Random;

import org.springframework.stereotype.Component;


@Component
public class RandomStringGenerator {

    private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";

    public String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        
        return sb.toString();
    }
}