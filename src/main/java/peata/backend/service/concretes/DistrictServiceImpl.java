package peata.backend.service.concretes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.District;
import peata.backend.repositories.DistrictRepository;
import peata.backend.service.abstracts.DistrictService;

@Service
public class DistrictServiceImpl implements DistrictService {

    @Autowired
    private DistrictRepository ilceRepository;


    public List<District> getAll(){
        return ilceRepository.findAll();
    }

    public District getById(Long id){
        return ilceRepository.findById(id).get();
    }   
    public List<District> getDistictbyCityId(Long cityId){
        return ilceRepository.findByCityId(cityId);
    }
    public List<District> getDistictbyCountryId(Long countryId){
        return ilceRepository.findByCountryId(countryId);
    }
}
