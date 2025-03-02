package peata.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import peata.backend.entity.District;
import peata.backend.service.abstracts.DistrictService;


@RestController
@RequestMapping("/district")
public class DistrictController {
    @Autowired
    private DistrictService districtService;
    
    
    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<List<District>> getAll() {

        List<District> districts = districtService.getAll();
        return ResponseEntity.ok(districts);
    }
    
    
    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<District> getById(@RequestParam("id") Long id) {

        District district = districtService.getById(id);
        return ResponseEntity.ok(district);
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getDistrictsByCityId")
    public ResponseEntity<List<District>> getDistrictsByCityId(@RequestParam("cityId") Long cityId) {
        List<District> districts = districtService.getDistictbyCityId(cityId);
        return ResponseEntity.ok(districts);
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getDistrictsByCountryId")
    public ResponseEntity<List<District>> getDistrictsByCountryId(@RequestParam("countryId") Long countryId) {
        List<District> districts = districtService.getDistictbyCountryId(countryId);
        return ResponseEntity.ok(districts);
    }

    
}







