package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.entity.City;

public interface CityService {

    public List<City> getAll();
    public City getById(Long id);
    public List<City> getCitiesByCountryId(Long countryId);
}