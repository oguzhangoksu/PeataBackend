package peata.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.Requests.AddRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("api/panel")
public class AdminPanelController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddService addService;

    @Autowired
    private S3Service s3Service;


    @Operation(summary = "Secured by ADMIN API", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )     
    @GetMapping("/user/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        return ResponseUtil.success("All users fetched successfully.", userService.allUsers());
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/user/getUsersWithPagination")
    public ResponseEntity<?> getUsersWithPagination(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        
        return ResponseUtil.success("Paginated users fetched successfully.", userService.getPaginatedUsers(page, size));
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/add/getAddsWithPagination")
    public ResponseEntity<?> getAddsWithPagination(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        return ResponseUtil.success("Paginated adds fetched successfully.", addService.getPaginatedAdds(page, size));
    }


    @Operation(summary = "Secured by ADMIN API ", 
    description = "This endpoint requires authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/add/getAllAdds")
    public ResponseEntity<?> getAllAdds() {
        return ResponseUtil.success("All adds fetched successfully.", addService.allAdds());
    }

    @Operation(summary = "Secured API ADMIN API ", 
        description = "This endpoint requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/add/delete")
    public ResponseEntity<?> delete(@RequestParam Long id) {
        addService.delete(id);
        return ResponseUtil.success("Add deleted");
    }

    @Operation(summary = "Secured API ADMIN API", 
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
                "                    user_id:user_id,\r\n\n" + //
                "                    add_type:add_type,\r\n\n" + //
                "                    status:status\r\n\n" + //
                "                    email:email\r\n" + //
                "                    phone:phone\r\n" + //
            
                "\n\nAdditional description: [Burda önemli olan bilgi şu kullanıcı resimleri değiştirmek isteyebilir bu durumda formData içerisine images boş bırakamıyoruz. Backend burda sıkıntı çıkartıyo. Bu duruumdan kurtulmak için eğer kullanıcı yeni bir resim yüklemediyse formData nın içerisinde images kısmına \"empty.txt\" file oluşturuyoruz backend gerisi hallediyo. Eğerki kullanıcı yeni bir resim eklediyse /add/save api'ındakiyle aynı mantıkta resimleri formdata'nın images kısmına ekliyoruz]",
        security = @SecurityRequirement(name = "bearerAuth")
    )    
    @PostMapping("/add/update")
    public ResponseEntity<?> update(@RequestParam("images") List<MultipartFile> files,@RequestParam("data") String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AddRequest addRequest = objectMapper.readValue(jsonData, AddRequest.class);
            User userDb=userService.findUserById(addRequest.getUser_id());
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
                addDb.setUser(userDb);
                return ResponseUtil.success("Add updated successfully.", addService.save(addDb,addDb.getUser()));
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
                        return ResponseUtil.error("File upload failed. " + file.getOriginalFilename() + " is not uploaded", null, HttpStatus.INTERNAL_SERVER_ERROR);
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
                addDb.setUser(userDb);
                return ResponseUtil.success("Add updated successfully.", addService.save(addDb,addDb.getUser()));
            }
        } catch (JsonProcessingException e) {
            return ResponseUtil.error("Invalid JSON data", null, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtil.error("File upload failed.", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
