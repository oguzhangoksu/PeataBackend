package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Binding;
import peata.backend.entity.User;
import peata.backend.entity.UserBindingSettings;
import peata.backend.repositories.UserBindingSettingsRepository;
import peata.backend.service.abstracts.UserBindingSettingsService;

@Service
public class UserBindingSettingsServiceImpl implements UserBindingSettingsService {

    @Autowired
    UserBindingSettingsRepository userBindingSettingsRepository;

    public void save(Binding binding, User user) {
        UserBindingSettings userBindingSettings = new UserBindingSettings();
        userBindingSettings.setBinding(binding);
        userBindingSettings.setUser(user);
        userBindingSettingsRepository.save(userBindingSettings);
    }

    public Boolean muteBinding(Binding binding, User user) {
        UserBindingSettings userBindingSettings = userBindingSettingsRepository.findByBindingAndUser(binding, user);
        if (userBindingSettings != null) {
            userBindingSettings.setIsMuted(true);
            userBindingSettingsRepository.save(userBindingSettings);
            return true;
        }
        return false;    
    }

    public Boolean unmuteBinding(Binding binding, User user) {
        UserBindingSettings userBindingSettings = userBindingSettingsRepository.findByBindingAndUser(binding, user);
        if (userBindingSettings != null) {
            userBindingSettings.setIsMuted(false);
            userBindingSettingsRepository.save(userBindingSettings);
            return true;
        }
        return false;    
    }
	
    public Boolean isMuted(Binding binding, User user) {
        UserBindingSettings userBindingSettings = userBindingSettingsRepository.findByBindingAndUser(binding, user);
        return userBindingSettings != null && Boolean.TRUE.equals(userBindingSettings.getIsMuted());
    }
}
