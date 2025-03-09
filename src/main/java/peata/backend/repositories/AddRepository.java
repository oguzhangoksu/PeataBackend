package peata.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import peata.backend.entity.Add;
import java.util.List;



public interface AddRepository extends JpaRepository<Add,Long>{

    Page<Add> findByStatus(int status,PageRequest pageRequest);
    List<Add> findByPcode(String pcode);
    List<Add> findByCountryId(int countryId);
    Page<Add> findByCountryId(int countryId,PageRequest pageRequest);
}
