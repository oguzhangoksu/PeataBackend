package peata.backend.service.concretes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Country;
import peata.backend.repositories.CountryRepository;
import peata.backend.service.abstracts.CountryService;


@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    private CountryRepository ulkelerRepository;


    public List<Country> getAll(){
        return ulkelerRepository.findAll();
    }

    public Country getById(Long id){
        return ulkelerRepository.findById(id).get();
    }   
}
