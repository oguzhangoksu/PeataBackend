package peata.backend.service.abstracts;

import peata.backend.entity.Binding;
import peata.backend.entity.User;

public interface UserBindingSettingsService {

    public void save(Binding binding, User user);
    public Boolean muteBinding(Binding binding, User user);
    public Boolean isMuted(Binding binding, User user);
    public Boolean unmuteBinding(Binding binding, User user);
} 
