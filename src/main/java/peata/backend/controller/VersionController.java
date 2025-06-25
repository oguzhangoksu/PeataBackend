package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import peata.backend.service.abstracts.VersionService;
import peata.backend.utils.ResponseUtil;

@RestController
@RequestMapping("api/version")
public class VersionController {

    @Autowired
    private VersionService versionService;

    @GetMapping("/validate/{version}")
    public ResponseEntity<?> validateVersion(@PathVariable String version) {
        try {
            boolean isValid = versionService.isValiadteVersion(version);
            return ResponseUtil.success("Version validation result.", isValid);
        } catch (Exception e) {
            return ResponseUtil.error("Version validation failed.");
        }
    }
    
}
