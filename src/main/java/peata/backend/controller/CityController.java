package peata.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import peata.backend.entity.City;
import peata.backend.service.abstracts.CityService;

@RestController
@RequestMapping("/city")

public class CityController {
    @Autowired
    private CityService cityService;


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<List<City>> getAll() {
        List<City> cities = cityService.getAll();
        return ResponseEntity.ok(cities);
    }


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<City> getById(@RequestParam("id") Long id) {
        City city = cityService.getById(id);
        return ResponseEntity.ok(city);
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getCitiesByCountryId")
    public ResponseEntity<List<City>> getCitiesByCountryId(@RequestParam("countryId") Long countryId) {
        List<City> cities = cityService.getCitiesByCountryId(countryId);
        return ResponseEntity.ok(cities);
    }

    
}


