package peata.backend.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import peata.backend.entity.User;
import peata.backend.service.abstracts.UserService;

import java.util.List;

@Component
public class ListenerInitializer {

    @Autowired
    private UserService userService; // Service to fetch user data

    @Autowired
    private DynamicListenerService dynamicListenerService; // Service to create listeners

    @PostConstruct
    public void init() {
        List<User> users = userService.allUsers(); // Fetch all users from your database
        for (User user : users) {
            dynamicListenerService.createListener(user.getCity(),user.getDistrict()); // Create the listener for the user
        }
    }
}
