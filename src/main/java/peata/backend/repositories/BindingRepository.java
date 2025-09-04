package peata.backend.repositories;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import peata.backend.entity.Binding;

public interface BindingRepository extends JpaRepository<Binding, Long> {
    
    Optional<Binding> findByAddIdAndOwnerIdAndRequesterId(Long addId, Long ownerId, Long requesterId);
    
    Optional<Binding> findById(Long bindingId);
    
    List<Binding> findByOwnerId(Long ownerId);
    
    List<Binding> findByRequesterId(Long requesterId);

    List<Binding> findAllByOwnerId(Long ownerId);

    List<Binding> findAllByRequesterId(Long requesterId);
}
