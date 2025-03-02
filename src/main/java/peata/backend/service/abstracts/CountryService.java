package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.entity.Country;

public interface CountryService {

    public Country getById(Long id);
    public List<Country> getAll();
    
}
