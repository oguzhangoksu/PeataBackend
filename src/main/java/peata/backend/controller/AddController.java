package peata.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.S3Service;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.FileData;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Requests.AddRequest;
import peata.backend.utils.Responses.AddResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;



@RestController
@RequestMapping("/add")
public class AddController {

    @Autowired
    private AddService addService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private UserService userService;

    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication. Data Json'nın stringe dönüştürülmüş hali "+
    " örn:\n const jsonData=JSON.stringify({\r\n" + //
                "                    animal_name:animal_name,\r\n" + //
                "                    age:age,\r\n" + //
                "                    breed:breed,\r\n" + //
                "                    type:type,\r\n" + //
                "                    gender:gender,\r\n" + //
                "                    description:description,\r\n" + //
                "                    city:city,\r\n" + //
                "                    district:district,\r\n" + //
                "                    add_type:add_type,\r\n" + //
                "                    user_id:user_id,\r\n" + //
                "                    status:status\r\n" + //
                "                    email:email\r\n" + //
                "                    phone:phone\r\n" + //
                "                })",
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
    public ResponseEntity<String> delete(@RequestParam Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String username = userPrincipal.getUsername();
        User userDb=userService.findUserByUsername(username);
        if (userDb == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        boolean addDeleted = false;
        Iterator<Add> iterator = userDb.getAds().iterator();
        while (iterator.hasNext()) {
            Add add = iterator.next();
            if (add.getId() != null && add.getId().equals(id)) {
                System.out.println("Removing ad with id: " + id);
                iterator.remove();
                
                boolean adExists = addService.existsById(id); 
                if (adExists) {
                    addService.delete(id);
                    addDeleted = true;
                } else {
                    return ResponseEntity.badRequest().body("Ad does not exist in the database");
                }
                break; 
            }
        }
        if (!addDeleted) {
            return ResponseEntity.badRequest().body("Ad ID not found in user's ads list");
        }
        return ResponseEntity.ok("Ad deleted");
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


    @Operation(summary = "Secured API ", 
        description = "This endpoint requires authentication. status=0(Pending),status=1(Rejected),status=2(Approved) ",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/status")
    public ResponseEntity<Page<AddDto>> getPaginatedAddswithStatus(@RequestParam int status,@RequestParam int page,@RequestParam int size) {
        Page<AddDto> addDtos=addService.getPaginatedAddswithStatus(status,page,size);
        return ResponseEntity.ok(addDtos);
    }


    
    @Operation(summary = "Secured API ", 
    description = "This endpoint requires authentication. Data Json'nın stringe dönüştürülmüş hali "+
    " örn:\n const jsonData=JSON.stringify({\r\n\n" + //
                "                    id:id                       " +
                "                    animal_name:animal_name,\r\n\n" + //
                "                    age:age,\r\n\n" + //
                "                    breed:breed,\r\n\n" + //
                "                    type:type,\r\n\n" + //
                "                    gender:gender,\r\n\n" + //
                "                    description:description,\r\n\n" + //
                "                    city:city,\r\n\n" + //
                "                    district:district,\r\n\n" + //
                "                    add_type:add_type,\r\n\n" + //
                "                    user_id:user_id,\r\n\n" + //
                "                    status:status\r\n\n" + //
                "                    email:email\r\n" + //
                "                    phone:phone\r\n" + //
                "\n\nAdditional description: [Burda önemli olan bilgi şu kullanıcı resimleri değiştirmek isteyebilir bu durumda formData içerisine images boş bırakamıyoruz. Backend burda sıkıntı çıkartıyo. Bu duruumdan kurtulmak için eğer kullanıcı yeni bir resim yüklemediyse formData nın içerisinde images kısmına \"empty.txt\" file oluşturuyoruz backend gerisi hallediyo. Eğerki kullanıcı yeni bir resim eklediyse /add/save api'ındakiyle aynı mantıkta resimleri formdata'nın images kısmına ekliyoruz]",
        security = @SecurityRequirement(name = "bearerAuth")
    )    
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestParam("images") List<MultipartFile> files,@RequestParam("data") String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AddRequest addRequest = objectMapper.readValue(jsonData, AddRequest.class);
            List<FileData> fileDatas= new ArrayList<FileData>();
            if(files.get(0).getOriginalFilename().equals("empty.txt")){
                Add addDb=addService.findAddByIdWithOutDto(addRequest.getId());
                addDb.setAge(addRequest.getAge());
                addDb.setAnimal_name(addRequest.getAnimal_name());
                addDb.setBreed(addRequest.getBreed());
                addDb.setCity(addRequest.getCity());
                addDb.setDescription(addRequest.getDescription());
                addDb.setDistrict(addRequest.getDistrict());
                addDb.setGender(addRequest.getGender());
                addDb.setStatus(addRequest.getStatus());
                addDb.setPhone(addRequest.getPhone());
                addDb.setEmail(addRequest.getEmail());
                return ResponseEntity.ok(addService.save(addDb,addDb.getUser()));
            }
            else{
                for (MultipartFile file : files) {
                    try {
                        FileData fileData = new FileData();
                        fileData.setFileName(file.getOriginalFilename());
                        fileData.setFileData(file.getBytes());
                        fileDatas.add(fileData);
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body("File upload failed."+file.getOriginalFilename()+" is not uploaded");
                    }
                }
                Add addDb=addService.findAddByIdWithOutDto(addRequest.getId());
                List<String> imageUrls=s3Service.uploadFilesToFolder(Long.toString(addRequest.getId()), fileDatas);
                addDb.setImages(imageUrls);
                addDb.setAge(addRequest.getAge());
                addDb.setAnimal_name(addRequest.getAnimal_name());
                addDb.setBreed(addRequest.getBreed());
                addDb.setCity(addRequest.getCity());
                addDb.setDescription(addRequest.getDescription());
                addDb.setDistrict(addRequest.getDistrict());
                addDb.setGender(addRequest.getGender());
                addDb.setStatus(addRequest.getStatus());
                addDb.setPhone(addRequest.getPhone());
                addDb.setEmail(addRequest.getEmail());
                return ResponseEntity.ok(addService.save(addDb,addDb.getUser()));
            }
            

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON data");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed.");
        }
        
    }
    
    

    

    






    

    
}
