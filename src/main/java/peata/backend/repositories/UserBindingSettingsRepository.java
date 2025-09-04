package peata.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.Binding;
import peata.backend.entity.User;
import peata.backend.entity.UserBindingSettings;

public interface UserBindingSettingsRepository extends JpaRepository<UserBindingSettings,Long>{

    public UserBindingSettings findByBindingAndUser(Binding binding, User user);
    
}
