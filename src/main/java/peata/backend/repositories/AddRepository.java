package peata.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.Add;


public interface AddRepository extends JpaRepository<Add,Long>{

    Page<Add> findByStatus(int status,PageRequest pageRequest);
}
