package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.UserService;

import java.util.List;

@RestController()
@RequestMapping("/panel")
public class AdminPanelController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddService addService;


    @Operation(summary = "Secured by ADMIN API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )     
    @GetMapping("/user/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.allUsers());
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/user/getUsersWithPagination")
    public ResponseEntity<Page<User>> getUsersWithPagination(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(userService.getPaginatedUsers(page, size));
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/add/getAddsWithPagination")
    public ResponseEntity<Page<AddDto>> getAddsWithPagination(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(addService.getPaginatedAdds(page, size));
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/add/getAllAdds")
    public ResponseEntity<List<Add>> getAllAdds() {
        return ResponseEntity.ok(addService.allAdds());
    }


}
