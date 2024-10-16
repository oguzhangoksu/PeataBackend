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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AddServiceImpl implements AddService{

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
        List<String> imageUrls=s3Service.uploadFilesToFolder(Long.toString(addDb.getId()), fileDatas);
        if(add.getStatus()==0){
            emailServiceImpl.sendToAdmins(owner.getEmail(), imageUrls, addDb.getId());
        }
        addDb.setImages(imageUrls);
        return addRepository.save(addDb);
    }
    public Add save(Add add,User user){
        if(add.getStatus()==2){
            notificationServiceImpl.sendNotification(user.getEmail(), add.getCity(), add.getDistrict(),add.getImages(), add.getAdd_type());
        }
        return addRepository.save(add);
    }
    
    
    public void delete(Long id){
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        s3Service.deleteFolder(""+add.getId()+"/");
        addRepository.delete(add);;
    }
    
    public AddDto findAddById(Long id){
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return convertToDto(add);
    }

    public Add findAddByIdWithOutDto(Long id){
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return add;
    }

    public List<String> findImagesByAddId(Long id){
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));

        return add.getImages();
    }

    public List<Add> allAdds(){
        List<Add> addDb= addRepository.findAll();
        return addDb;
    }

    public Page<AddDto> getPaginatedAdds(int page, int size) {
        Page<Add> adds = addRepository.findAll(PageRequest.of(page, size));
        return adds.map(this::convertToDto);
    }

    public Page<AddDto> getPaginatedAddswithStatus(int status,int page, int size){
        Page<Add> adds = addRepository.findByStatus(status,PageRequest.of(page, size));

        return adds.map(this::convertToDto);
    }


    public boolean existsById(Long id) {
        // Use the repository's existsById method to check for existence
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


