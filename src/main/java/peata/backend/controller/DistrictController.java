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
import peata.backend.utils.ResponseUtil;


@RestController
@RequestMapping("api/district")
public class DistrictController {
    @Autowired
    private DistrictService districtService;
    
    
    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<District> districts = districtService.getAll();
            return ResponseUtil.success("Districts fetched successfully.", districts);
        } catch (Exception e) {
            return ResponseUtil.error("Districts could not be fetched.");
        }
    }
    
    
    @Operation(summary = "Public API", description = "") 
    @GetMapping("/getById")
    public ResponseEntity<?> getById(@RequestParam("id") Long id) {
        try {
            District district = districtService.getById(id);
            if (district == null) {
                return ResponseUtil.error("District not found.");
            }
            return ResponseUtil.success("District fetched successfully.", district);
        } catch (Exception e) {
            return ResponseUtil.error("District could not be fetched.");
        }
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getDistrictsByCityId")
    public ResponseEntity<?> getDistrictsByCityId(@RequestParam("cityId") Long cityId) {
        try {
            List<District> districts = districtService.getDistictbyCityId(cityId);
            return ResponseUtil.success("Districts by city fetched successfully.", districts);
        } catch (Exception e) {
            return ResponseUtil.error("Districts by city could not be fetched.");
        }
    }

    @Operation(summary = "Public API", description = "")
    @GetMapping("/getDistrictsByCountryId")
    public ResponseEntity<?> getDistrictsByCountryId(@RequestParam("countryId") Long countryId) {
        try {
            List<District> districts = districtService.getDistictbyCountryId(countryId);
            return ResponseUtil.success("Districts by country fetched successfully.", districts);
        } catch (Exception e) {
            return ResponseUtil.error("Districts by country could not be fetched.");
        }
    }

    
}







