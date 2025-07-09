package peata.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import peata.backend.entity.Binding;

public interface BindingRepository extends JpaRepository<Binding, Long> {
    

}
