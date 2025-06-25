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
import peata.backend.utils.ResponseUtil;

@RestController
@RequestMapping("api/city")

public class CityController {
    @Autowired
    private CityService cityService;


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<City> cities = cityService.getAll();
            return ResponseUtil.success("Cities fetched successfully.", cities);
        } catch (Exception e) {
            return ResponseUtil.error("Cities could not be fetched.");
        }
    }


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<?> getById(@RequestParam("id") Long id) {
        try {
            City city = cityService.getById(id);
            if (city == null) {
                return ResponseUtil.error("City not found.");
            }
            return ResponseUtil.success("City fetched successfully.", city);
        } catch (Exception e) {
            return ResponseUtil.error("City could not be fetched.");
        }
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getCitiesByCountryId")
    public ResponseEntity<?> getCitiesByCountryId(@RequestParam("countryId") Long countryId) {
        try {
            List<City> cities = cityService.getCitiesByCountryId(countryId);
            return ResponseUtil.success("Cities by country fetched successfully.", cities);
        } catch (Exception e) {
            return ResponseUtil.error("Cities by country could not be fetched.");
        }
    }

    
}


