package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.repositories.AddRepository;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.S3Service;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.FileData;
import peata.backend.utils.Requests.AddRequest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AddServiceImpl implements AddService{

    private static final Logger logger = LoggerFactory.getLogger(AddServiceImpl.class);

    @Autowired
    private AddRepository addRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;


    public Add save(AddRequest addRequest,List<FileData> fileDatas) throws IOException{
        logger.info("Saving new ad for user with ID: {}", addRequest.getUser_id());
        Add add = new Add();
        add.setAnimal_name(addRequest.getAnimal_name());
        add.setAge(addRequest.getAge());
        add.setBreed(addRequest.getBreed());
        add.setType(addRequest.getType());
        add.setGender(addRequest.getGender());
        add.setDescription(addRequest.getDescription());
        add.setImages(new ArrayList<String>());
        add.setCity(addRequest.getCity());
        add.setDistrict(addRequest.getDistrict());
        add.setDate(new Date());
        add.setStatus(addRequest.getStatus());
        add.setAdd_type(addRequest.getAdd_type());
        add.setPhone(addRequest.getPhone());
        add.setEmail(addRequest.getEmail());
        User owner=userService.findUserById(addRequest.getUser_id());
        add.setUser(owner);
        Add addDb=addRepository.save(add);
        logger.debug("Ad saved with ID: {}", addDb.getId());
        List<String> imageUrls=s3Service.uploadFilesToFolder(Long.toString(addDb.getId()), fileDatas);
        logger.info("Uploaded {} images for ad ID: {}", imageUrls.size(), addDb.getId());
        if(add.getStatus()==0){
            emailServiceImpl.sendToAdmins(owner.getEmail(), imageUrls, addDb.getId());
            logger.info("Notification email sent to admins for ad ID: {}", addDb.getId());
        }
        addDb.setImages(imageUrls);
        return addRepository.save(addDb);
    }
    public Add save(Add add,User user){
        logger.info("Updating ad with ID: {}", add.getId());
        if(add.getStatus()==2){
            notificationServiceImpl.sendNotification(user.getEmail(), add.getCity(), add.getDistrict(),add.getImages(), add.getAdd_type());
            logger.info("Notification sent to user: {} for ad ID: {}", user.getEmail(), add.getId());
        }
        return addRepository.save(add);
    }
    
    
    public void delete(Long id){
        logger.info("Deleting ad with ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        s3Service.deleteFolder(""+add.getId()+"/");
        addRepository.delete(add);;
        logger.info("Ad with ID: {} has been deleted", id);
    }
    
    public AddDto findAddById(Long id){
        logger.info("Fetching ad with ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return convertToDto(add);
    }

    public Add findAddByIdWithOutDto(Long id){
        logger.info("Fetching ad without DTO for ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return add;
    }

    public List<String> findImagesByAddId(Long id){
        logger.info("Fetching images for ad ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));

        return add.getImages();
    }

    public List<Add> allAdds(){
        logger.info("Fetching all ads from the database");
        List<Add> addDb= addRepository.findAll();
        return addDb;
    }

    public Page<AddDto> getPaginatedAdds(int page, int size) {
        logger.info("Fetching paginated ads: page {}, size {}", page, size);
        Page<Add> adds = addRepository.findAll(PageRequest.of(page, size));
        return adds.map(this::convertToDto);
    }

    public Page<AddDto> getPaginatedAddswithStatus(int status,int page, int size){
        logger.info("Fetching paginated ads with status {}: page {}, size {}", status, page, size);
        Page<Add> adds = addRepository.findByStatus(status,PageRequest.of(page, size));

        return adds.map(this::convertToDto);
    }


    public boolean existsById(Long id) {
        logger.info("Checking if ad exists with ID: {}", id);
        return addRepository.existsById(id);
    }


    private AddDto convertToDto(Add add) {
        AddDto dto = new AddDto();
        dto.setId(add.getId());
        dto.setAnimal_name(add.getAnimal_name());
        dto.setAge(add.getAge());
        dto.setBreed(add.getBreed());
        dto.setType(add.getType());
        dto.setGender(add.getGender());
        dto.setDescription(add.getDescription());
        dto.setImages(add.getImages());
        dto.setCity(add.getCity());
        dto.setDistrict(add.getDistrict());
        dto.setDate(add.getDate());
        dto.setAdd_type(add.getAdd_type());
        dto.setPhone(add.getPhone());
        dto.setEmail(add.getEmail());
        dto.setStatus(add.getStatus());
        dto.setUser_id(add.getUser().getId());
        return dto;
    }

   

}


