package peata.backend.service.concretes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.City;
import peata.backend.repositories.CityRepository;
import peata.backend.service.abstracts.CityService;

@Service
public class CityServiceImpl implements CityService {

    @Autowired
    private CityRepository sehirlerRepository;


    public List<City> getAll(){
        return sehirlerRepository.findAll();
    }

    public City getById(Long id){
        return sehirlerRepository.findById(id).get();
    }   
    public List<City> getCitiesByCountryId(Long countryId){
        return sehirlerRepository.findByCountryId(countryId);
    }
    
}
