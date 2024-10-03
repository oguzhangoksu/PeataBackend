package peata.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.service.abstracts.AddService;
import peata.backend.utils.FileData;
import peata.backend.utils.Requests.AddRequest;
import peata.backend.utils.Responses.AddResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



@RestController
@RequestMapping("/add")
public class AddController {

    @Autowired
    private AddService addService;


    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @PostMapping("/save")
    public ResponseEntity<?> handleFileUpload(@RequestParam("images") List<MultipartFile> files,@RequestParam("data") String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AddRequest addRequest = objectMapper.readValue(jsonData, AddRequest.class);
            List<FileData> fileDatas= new ArrayList<FileData>();
           
            for (MultipartFile file : files) {
                try {
                    FileData fileData = new FileData();
                    // Get the filename
                    fileData.setFileName(file.getOriginalFilename());
                    fileData.setFileData(file.getBytes());
                    fileDatas.add(fileData);
        
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(500).body("File upload failed."+file.getOriginalFilename()+" is not uploaded");
                }
            }
            Add addDb =addService.save(addRequest,fileDatas);
            
            return ResponseEntity.ok(new AddResponse("Add saved:",addDb.getId(),addDb.getImages()));
            
            
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON data");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed.");
        }
        
    }


    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/findAddById")
    public ResponseEntity<AddDto> findAddById(@RequestParam Long id) {
        AddDto addDto=addService.findAddById(id);
        return ResponseEntity.ok(addDto);
    }

    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/findImagesByAddId")
    public ResponseEntity<List<String>> findImagesByAddId(@RequestParam Long id) {
        List<String> images=addService.findImagesByAddId(id);
        return ResponseEntity.ok(images);
    }


    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/getPaginatedAdds")
    public ResponseEntity<Page<AddDto>> getPaginatedAdds(@RequestParam int page,@RequestParam int size) {
        Page<AddDto> addDtos=addService.getPaginatedAdds(page,size);
        return ResponseEntity.ok(addDtos);
    }

    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/delete")
    public ResponseEntity<String> getPaginatedAdds(@RequestParam Long id) {
        addService.delete(id);
        return ResponseEntity.ok("Add deleted");
    }

    
    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @PostMapping("/getAddsWithIds")
    public ResponseEntity<List<AddDto>> getAddsWithIds(@RequestParam List<Long> idsList) {
        List<AddDto> addsList= new ArrayList<>();
        for(int i=0;i<idsList.size();i++){
            addsList.add(addService.findAddById(idsList.get(i)));    
        }
        return ResponseEntity.ok(addsList);
    }


    






    

    
}
