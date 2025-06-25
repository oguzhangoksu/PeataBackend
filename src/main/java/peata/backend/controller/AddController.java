package peata.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import peata.backend.dtos.ActivityLogDto;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.S3Service;
import peata.backend.service.abstracts.UserService;
import peata.backend.service.concretes.ActivityLogServiceImpl;
import peata.backend.utils.FileData;
import peata.backend.utils.ResponseUtil;
import peata.backend.utils.UserPrincipal;
import peata.backend.utils.Enums.ActivityType;
import peata.backend.utils.Requests.AddComplaint;
import peata.backend.utils.Requests.AddRequest;
import peata.backend.utils.Requests.DeleteImageRequest;
import peata.backend.utils.Requests.DeleteReason;
import peata.backend.utils.Requests.UpdateAddInfoRequest;
import peata.backend.utils.Responses.AddResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;



@RestController
@RequestMapping("api/add")
public class AddController {

    private static final Logger logger = LoggerFactory.getLogger(AddController.class);
    
    @Autowired
    private AddService addService;
    @Autowired
    private S3Service s3Service;

    @Lazy
    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogServiceImpl activityLogServiceImpl;

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
                "                    status:status\r\n" + //
                "                    email:email\r\n" + //
                "                    phone:phone\r\n" + //
                "                    countryId:countryId\r\n" + //
                "                })",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @PostMapping("/save")
    public ResponseEntity<?> handleFileUpload(@RequestParam("images") List<MultipartFile> files,@RequestParam("data") String jsonData, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ObjectMapper objectMapper = new ObjectMapper();
        String username= userPrincipal.getUsername();
        User userDb=userService.findUserByUsername(username);

        try {
            AddRequest addRequest = objectMapper.readValue(jsonData, AddRequest.class);
            addRequest.setUser_id(userDb.getId());
            List<FileData> fileDatas= new ArrayList<FileData>();
            for (MultipartFile file : files) {
                try {
                    FileData fileData = new FileData();
                    fileData.setFileName(file.getOriginalFilename());
                    fileData.setFileData(file.getBytes());
                    fileDatas.add(fileData);
                } catch (IOException e) {
                    logger.error("File upload failed for file: {}", file.getOriginalFilename(), e);
                    return ResponseUtil.error("File upload failed."+file.getOriginalFilename()+" is not uploaded");
                }
            }
            Add addDb =addService.save(addRequest,fileDatas);
            logger.info("Ad saved successfully with ID: {} , {}", addDb.getId(),username);
            return ResponseUtil.success("Add saved", new AddResponse("Add saved:",addDb.getId(),addDb.getImages()));
        } catch (JsonProcessingException e) {
            logger.error("Invalid JSON data received: {}", jsonData, e);
            return ResponseUtil.error("Invalid JSON data");
        } catch (IOException e) {
            logger.error("File upload failed.", e);
            return ResponseUtil.error("File upload failed.");
        }
    }

    @Operation(summary = "Secured API ", 
    description = "Retrieves an add based on its ID. This endpoint requires user authentication.",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/findAddById")
    public ResponseEntity<?> findAddById(@RequestParam Long id) {
        AddDto addDto=addService.findAddDtoById(id);
        logger.info("Successfully retrieved ad: {}", addDto.getId());
        return ResponseUtil.success("Ad retrieved", addDto);
    }
    
    @Operation(summary = "Secured API ", 
        description = "Retrieves images associated with a specific ad using its ID. This endpoint also requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/findImagesByAddId")
    public ResponseEntity<?> findImagesByAddId(@RequestParam Long id) {
        List<String> images=addService.findImagesByAddId(id);
        logger.info("Successfully retrieved images for ad ID {}: {}", id, images);
        return ResponseUtil.success("Images retrieved", images);
    }

    @Operation(summary = "Public API ", 
        description = "Fetches a paginated list of adds. User authentication is required to access this endpoint.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/getPaginatedAdds")
    public ResponseEntity<?> getPaginatedAdds(@RequestParam int page,@RequestParam int size) {
        Page<AddDto> addDtos=addService.getPaginatedAdds(page,size);
        logger.info("Successfully retrieved {} adds for page {} with size {}", addDtos.getTotalElements(), page, size);
        return ResponseUtil.success("Paginated adds retrieved", addDtos);
    }

    @Operation(summary = "Public API ", 
    description = "Fetches a paginated list of adds based on their status (e.g., Pending, Rejected, Approved). User authentication is required. status=0(Pending),status=1(Rejected),status=2(Approved) ",
    security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/getPaginatedAddsByCountryId")
    public ResponseEntity<?> getPaginatedAddswithCountryId(@RequestParam int countryId,@RequestParam int page,@RequestParam int size) {
        Page<AddDto> addDtos = addService.getPaginatedAddswithCountryId(countryId, page, size);
        logger.info("Received request for paginated adds with countryId: {}, page: {}, size: {}", countryId, page, size);
        return ResponseUtil.success("Paginated adds by countryId retrieved", addDtos);
    }


    @Operation(summary = "Secured API ", 
        description = "Deletes an ad based on its ID. This endpoint requires user authentication and checks if the ad belongs to the authenticated user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String username = userPrincipal.getUsername();
        User userDb=userService.findUserByUsername(username);
        if (userDb == null) {
            logger.warn("User not found: {}", username);
            return ResponseUtil.error("User not found");
        }
        boolean addDeleted = false;
        Iterator<Add> iterator = userDb.getAds().iterator();
        while (iterator.hasNext()) {
            Add add = iterator.next();
            if (add.getId() != null && add.getId().equals(id)) {
                iterator.remove();
                boolean adExists = addService.existsById(id); 
                if (adExists) {
                    addService.delete(id);
                    addDeleted = true;
                } else {
                    logger.warn("Ad does not exist in the database with ID: {}", id);
                    return ResponseUtil.error("Ad does not exist in the database");
                }
                break; 
            }
        }
        if (!addDeleted) {
            logger.warn("Ad ID {} not found in user's ads list for user: {}", id, username);
            return ResponseUtil.error("Ad ID not found in user's ads list");
        }
        logger.info("Ad deleted successfully with ID: {} by {}", id , username);
        return ResponseUtil.success("Ad deleted");
    }
    
    @Operation(
        summary = "Public API", 
        description = "Fetches one or more ads based on the provided PCode.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/findAddByPcode")
    public ResponseEntity<?> findAddByPcode(@RequestParam String pCode) {
        AddDto addDto = addService.findAddByPcode(pCode);
        if (addDto == null) {
            logger.warn("No ad found for PCode: {}", pCode);
            return ResponseUtil.error("No ad found for PCode: " + pCode);
        }
        logger.info("Successfully retrieved ad with PCode: {}", pCode);
        return ResponseUtil.success("Ad retrieved by PCode", addDto);
    }

    
    @Operation(summary = "Secured API ", 
        description = "Retrieves multiple adds based on a list of provided IDs. User authentication is required for this operation.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @PostMapping("/getAddsWithIds")
    public ResponseEntity<?> getAddsWithIds(@RequestParam List<Long> idsList) {
        List<AddDto> addsList = new ArrayList<>();
        for (Long id : idsList) {
            try {
                AddDto addDb = addService.findAddDtoById(id);
                addsList.add(addDb);
            } catch (EntityNotFoundException e) {
                logger.warn("Add not found for ID: {}", id);
                continue; 
            } catch (Exception e) {
                logger.error("An error occurred while fetching add with ID: {}. Error: {}", id, e.getMessage());
            }
        }
        logger.info("Returning list of Favorite Adds: ");
        return ResponseUtil.success("Favorite adds retrieved", addsList);
    }


    @Operation(summary = "Secured API ", 
        description = "Fetches a paginated list of adds based on their status (e.g., Pending, Rejected, Approved). User authentication is required. status=0(Pending),status=1(Rejected),status=2(Approved) ",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @GetMapping("/status")
    public ResponseEntity<?> getPaginatedAddswithStatus(@RequestParam int status,@RequestParam int page,@RequestParam int size) {
        Page<AddDto> addDtos = addService.getPaginatedAddswithStatus(status, page, size);
        logger.info("Returning paginated adds with status: {} , page:{}, size: {}", status, page, size);
        return ResponseUtil.success("Paginated adds by status retrieved", addDtos);
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
                "                    status:status\r\n\n" + //
                "                    email:email\r\n" + //
                "                    phone:phone\r\n" + //
            
                "\n\nAdditional description: [Burda önemli olan bilgi şu kullanıcı resimleri değiştirmek isteyebilir bu durumda formData içerisine images boş bırakamıyoruz. Backend burda sıkıntı çıkartıyo. Bu duruumdan kurtulmak için eğer kullanıcı yeni bir resim yüklemediyse formData nın içerisinde images kısmına \"empty.txt\" file oluşturuyoruz backend gerisi hallediyo. Eğerki kullanıcı yeni bir resim eklediyse /add/save api'ındakiyle aynı mantıkta resimleri formdata'nın images kısmına ekliyoruz]",
        security = @SecurityRequirement(name = "bearerAuth")
    )    
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestParam("images") List<MultipartFile> files,@RequestParam("data") String jsonData) {
        logger.info("Received request to update add with JSON data: {}", jsonData);
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
                return ResponseUtil.success("Add updated", addService.save(addDb,addDb.getUser()));
            }
            else{
                for (MultipartFile file : files) {
                    try {
                        FileData fileData = new FileData();
                        fileData.setFileName(file.getOriginalFilename());
                        fileData.setFileData(file.getBytes());
                        fileDatas.add(fileData);
                        logger.info("File uploaded: {}", file.getOriginalFilename());
                    } catch (IOException e) {
                        logger.error("File upload failed: {}. Error: {}", file.getOriginalFilename(), e.getMessage());
                        return ResponseUtil.error("File upload failed."+file.getOriginalFilename()+" is not uploaded");
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
                logger.info("Updated add with ID: {} and uploaded new images.", addRequest.getId());
                return ResponseUtil.success("Add updated", addService.save(addDb,addDb.getUser()));
            }
        } catch (JsonProcessingException e) {
            logger.error("Invalid JSON data. Error: {}", e.getMessage());
            return ResponseUtil.error("Invalid JSON data");
        } catch (IOException e) {
            logger.error("File upload failed. Error: {}", e.getMessage());
            return ResponseUtil.error("File upload failed.");
        }
    }

    @Operation(summary = "Secured API ", 
        description = "This API endpoint deletes specified images from an advertisement if the authenticated user is the owner of the advertisement.",
        security = @SecurityRequirement(name = "bearerAuth")
    )   
    @PostMapping("/update/deleteImages")
    public ResponseEntity<?> deleteImages(@RequestBody DeleteImageRequest deleteImageRequest,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user =userService.findUserByUsername(userPrincipal.getUsername());
        AddDto addDto=addService.findAddDtoById(deleteImageRequest.getAddId());
        if(addDto.getUser_id() == user.getId()){
            addService.deleteImage(addDto, deleteImageRequest.getImages());
            return ResponseUtil.success("Images deleted successfully. for addId: "+deleteImageRequest.getAddId());
        }
        else{
            return ResponseUtil.error("This add is not your own. You can't delete images.");
        }
    }

    @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows an authenticated user to add specified images, provided as a List<MultipartFile>, to an advertisement if they are the owner of the advertisement",
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/update/addImages")
    public ResponseEntity<?> addImages(@RequestParam("images") List<MultipartFile> files,@RequestParam("addId") Long addId,@AuthenticationPrincipal UserPrincipal userPrincipal) throws IOException {
        User user =userService.findUserByUsername(userPrincipal.getUsername());
        AddDto addDto=addService.findAddDtoById(addId);
        if(addDto.getUser_id() == user.getId()){
            logger.info("User {} is adding {} images to adId {}", user.getUsername(), files.size(), addId);
            addService.addImage(addDto, files);
            return ResponseUtil.success("Images added successfully.");
        }
        else{
            logger.info("Unauthorized attempt: User {} trying to add images for adId {} which is not owned by them.", user.getUsername(), addId);
            return ResponseUtil.error("This add is not your own. You can't add images.");
        }
    }

    @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows an authenticated user to update the information of an advertisement if they are the owner of that advertisement.",
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/update/Info")
    public ResponseEntity<?> addInfo(UpdateAddInfoRequest addInfoRequest,@AuthenticationPrincipal UserPrincipal userPrincipal)  {
        User user =userService.findUserByUsername(userPrincipal.getUsername());
        if(addInfoRequest.getUser_id() == user.getId()){
            addService.updateAddDto(addInfoRequest);
            logger.info("User {} successfully updated ad info for ad id: {}", user.getUsername(), addInfoRequest.getId());
            return ResponseUtil.success("Add Info updated successfully.");
        }
        else{
            logger.warn("Unauthorized update attempt: User {} tried to update ad info for an ad not owned by them.", user.getUsername());
            return ResponseUtil.error("This add is not your own. You can't update add.");
        }
    }

        @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows an authenticated user to delete an advertisement with a specified reason if they are the owner of that advertisement.",
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/delete/reason")
    public ResponseEntity<?> deleteReason(@AuthenticationPrincipal UserPrincipal userPrincipal,DeleteReason deleteReason)  {
        Boolean isOwned =  userService.isOwenedAdd(userPrincipal.getUsername(),deleteReason.getAddId());
        if(isOwned){
            addService.delete(deleteReason.getAddId());
            logger.info("User {} successfully deleted ad with ID: {}", userPrincipal.getUsername(), deleteReason.getAddId());
            return ResponseUtil.success("Add deleted successfully. Reason: " + deleteReason.getDescription());
        }
        else{
            logger.warn("Unauthorized delete attempt: User {} tried to delete ad with ID: {} which is not owned by them.", userPrincipal.getUsername(), deleteReason.getAddId());
            return ResponseUtil.error("This add is not your own. You can't delete add.");
        }
    }

    @Operation(summary = "Secured API ", 
    description = "\n" + //
                "This API endpoint allows an authenticated user to delete an advertisement with a specified reason if they are the owner of that advertisement.",
    security = @SecurityRequirement(name = "bearerAuth"))   
    @PostMapping("/complaint")
    public ResponseEntity<?> complaint(@AuthenticationPrincipal UserPrincipal userPrincipal,AddComplaint addComplaint)  {
        Add add =  addService.findAddById(addComplaint.getAddId());
        ActivityLogDto activityLogDto = new ActivityLogDto();
        activityLogDto.setContent(addComplaint.getDescription());
        activityLogDto.setActivityType(ActivityType.COMPLAINT);
        User user =userService.findUserByUsername(userPrincipal.getUsername());
        if(add != null && add.getUser().getUsername().equals(userPrincipal.getUsername())){
            activityLogServiceImpl.saveActivityLog(activityLogDto,user,add);
            logger.info("User {} successfully filed a complaint for ad with ID: {}", userPrincipal.getUsername(), addComplaint.getAddId());
            return ResponseUtil.success("Complaint filed successfully for ad ID: " + addComplaint.getAddId());
        }
        else{
            logger.warn("Unauthorized delete attempt: User {} tried to delete ad with ID: {} which is not owned by them.", userPrincipal.getUsername(), addComplaint.getAddId());
            return ResponseUtil.error("This ad does not belong to you. You cannot file a complaint.");
        }
    }
  
    
}
