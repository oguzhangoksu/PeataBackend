package peata.backend.core;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/firebaseJson/firebase-service-account.json");

        return GoogleCredentials.fromStream(serviceAccount)
                .createScoped("https://www.googleapis.com/auth/firebase.messaging");
    }
}