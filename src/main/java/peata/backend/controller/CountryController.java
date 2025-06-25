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
import peata.backend.utils.ResponseUtil;

@RestController
@RequestMapping("api/country")
public class CountryController {
    
    @Autowired
    private CountryService countryService;


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Country> countries = countryService.getAll();
            return ResponseUtil.success("Countries fetched successfully.", countries);
        } catch (Exception e) {
            return ResponseUtil.error("Countries could not be fetched.");
        }
    }


    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<?> getById(@RequestParam("id") Long id) {
        try {
            Country country = countryService.getById(id);
            if (country == null) {
                return ResponseUtil.error("Country not found.");
            }
            return ResponseUtil.success("Country fetched successfully.", country);
        } catch (Exception e) {
            return ResponseUtil.error("Country could not be fetched.");
        }
    }
    

}
