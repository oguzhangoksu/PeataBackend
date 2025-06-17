package peata.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import peata.backend.entity.Country;
import peata.backend.service.abstracts.CountryService;

@RestController
@RequestMapping("api/country")
public class CountryController {
    
    @Autowired
    private CountryService countryService;


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<List<Country>> getAll() {
        List<Country> countries = countryService.getAll();
        return ResponseEntity.ok(countries);
    }


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<Country> getById(@RequestParam("id") Long id) {
        Country country = countryService.getById(id);
        return ResponseEntity.ok(country);
    }
    

}
