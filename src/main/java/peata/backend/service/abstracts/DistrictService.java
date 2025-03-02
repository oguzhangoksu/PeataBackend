package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.entity.District;

public interface DistrictService {
    
    public List<District> getAll();
    public District getById(Long id);
    public List<District> getDistictbyCityId(Long cityId);
    public List<District> getDistictbyCountryId(Long countryId);
}
