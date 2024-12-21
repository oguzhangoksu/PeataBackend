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
    private UserService userService; 

    @Autowired
    private DynamicListenerService dynamicListenerService; 

    @PostConstruct
    public void init() {
        List<User> users = userService.allUsers(); 
        for (User user : users) {
            dynamicListenerService.createListener(user.getCity(),user.getDistrict()); 
        }
    }
}
