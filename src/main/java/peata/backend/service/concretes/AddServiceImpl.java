package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.repositories.AddRepository;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.S3Service;
import peata.backend.service.abstracts.UserService;
import peata.backend.utils.FileData;
import peata.backend.utils.RandomStringGenerator;
import peata.backend.utils.Requests.AddRequest;
import peata.backend.utils.Requests.UpdateAddInfoRequest;


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

    @Autowired
    private RandomStringGenerator randomStringGenerator;

   


    public Add save(AddRequest addRequest,List<FileData> fileDatas) throws IOException{
        logger.info("Saving new ad for user with ID: {}", addRequest.getUser_id());
        String pCode = randomStringGenerator.generateRandomString(5);
        List<Add> addList = addRepository.findByPcode(pCode);
        while(addList.size()>0){
            pCode = randomStringGenerator.generateRandomString(5);
            addList = addRepository.findByPcode(pCode);
        }
        Add add = new Add();
        add.setAnimal_name(addRequest.getAnimal_name());
        add.setAge(addRequest.getAge());
        add.setPcode(pCode);
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
        add.setCountryId(addRequest.getCountryId());
        User owner=userService.findUserById(addRequest.getUser_id());
        add.setUser(owner);
        Add addDb=addRepository.save(add);
        logger.debug("Ad saved with ID: {}", addDb.getId());
        List<String> imageUrls=s3Service.uploadFilesToFolder(Long.toString(addDb.getId()), fileDatas);
        logger.info("Uploaded {} images for ad ID: {}", imageUrls.size(), addDb.getId());
        if(add.getStatus()==0){
            emailServiceImpl.sendToAdmins(owner.getEmail(), imageUrls, addDb.getId(),addDb.getPcode(),owner.getLanguage());
            logger.info("Notification email sent to admins for ad ID: {}", addDb.getId());
        }
        addDb.setImages(imageUrls);
        return addRepository.save(addDb);
    }
    //update
    public Add save(Add add,User user){
        logger.info("Updating ad with ID: {}", add.getId());
        if(add.getStatus()==2){
            notificationServiceImpl.sendNotification(user.getEmail(), add.getCity(), add.getDistrict(),add.getImages(), add.getAdd_type(),add.getPcode(),user.getLanguage());
            logger.info("Notification sent to user: {} for ad ID: {}", user.getEmail());
        }
        return addRepository.save(add);
    }
    
    
    public void delete(Long id){
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        // s3Service.deleteFolder(""+add.getId()+"/");
        deleteById(add);
        logger.info("Ad with ID: {} has been deleted", id);
    }

    public void deleteById(Add add) {
        logger.info("Deleting ad with ID: {}", add.getId());
        add.setActive(false);
        Add dbAdd = addRepository.save(add);
        if (dbAdd == null) {
            throw new EntityNotFoundException("Add with ID " + add.getId() + " not found");
        }
    }

    public boolean insertAddComplaint(Long addId, String complaint) {
        logger.info("Inserting complaint for ad ID: {}", addId);
        Add add = addRepository.findById(addId)
            .orElseThrow(() -> new EntityNotFoundException("Add with ID " + addId + " not found"));
        if(add != null) {

        }
     
        return true;
    }
    
    public AddDto findAddDtoById(Long id){
        logger.info("Fetching ad with ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return convertToDto(add);
    }

    public Add findAddById(Long id){
        logger.info("Fetching ad with ID: {}", id);
        Add add =addRepository.findById(id)
            .orElseThrow(()-> new EntityNotFoundException("Add with ID " + id + " not found"));
        return add;
    }
    
    
    public AddDto findAddByPcode(String pCode){
        logger.info("Fetching ad with Pcode: {}", pCode);
        List<Add> addList =addRepository.findByPcode(pCode);
        if(addList.size()!=0){
            Add add = addList.get(0);
            return convertToDto(add);
        }
        else{
            throw new EntityNotFoundException("Add with Pcode " + pCode + " not found");
        }
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
    
    public List<AddDto> findAddsbyCountryId(int countryId){
        logger.info("Fetching ads with country ID: {}", countryId);
        List<Add> addList =addRepository.findByCountryId(countryId);
        List<AddDto> addDtos = new ArrayList<>();
        for(Add add : addList){
            addDtos.add(convertToDto(add));
        }
        return addDtos;
    }
    public Page<AddDto> getPaginatedAddswithCountryId(int countryId,int page, int size){
        logger.info("Fetching paginated ads with countryId {}: page {}, size {}", countryId, page, size);
        Page<Add> adds = addRepository.findByCountryId(countryId,PageRequest.of(page, size));

        return adds.map(this::convertToDto);
    }

    public boolean existsById(Long id) {
        logger.info("Checking if ad exists with ID: {}", id);
        return addRepository.existsById(id);
    }

    public void deleteImage(AddDto addDto, List<String> images) {
        Add add = addRepository.findById(addDto.getId())
           .orElseThrow(() -> new EntityNotFoundException("Add with ID " + addDto.getId() + " not found"));
        for (String image : images) { 
            add.getImages().remove(image);
            logger.info("Deleting image {} for ad ID: {}", image, addDto.getId());
            List<String> result =extractFolderAndFileName(image);
            s3Service.deleteImageInFolder(result.get(0), result.get(1));
        }
        addRepository.save(add);
    }

    public List<String> addImage(AddDto addDto, List<MultipartFile> files) throws IOException {
        Add addDb = addRepository.findById(addDto.getId())
            .orElseThrow(() -> new EntityNotFoundException("Add with ID " + addDto.getId() + " not found"));
        List<FileData> fileDatas = new ArrayList<FileData>();
        for (MultipartFile file : files) {
            try {
                FileData fileData = new FileData();
                fileData.setFileName(file.getOriginalFilename());
                fileData.setFileData(file.getBytes());
                fileDatas.add(fileData);
                logger.info("File prepared for upload: {}", file.getOriginalFilename());
            } catch (IOException e) {
                logger.error("File upload failed for: {}. Error: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
    
        // Log the number of files prepared for upload
        logger.info("Total files prepared for upload: {}", fileDatas.size());
        List<String> imageUrls = s3Service.uploadFilesToFolder(Long.toString(addDto.getId()), fileDatas);
    
        for (String imageUrl : imageUrls) {
            addDb.getImages().add(imageUrl);
            logger.info("Image URL added to Add ID {}: {}", addDto.getId(), imageUrl);
        }
        addRepository.save(addDb);
        return addDb.getImages();
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
        dto.setPCode(add.getPcode());
        dto.setCountryId(add.getCountryId());
        return dto;
    }

    public Add updateAddDto(UpdateAddInfoRequest addInfoRequest){
        Add addDb = addRepository.findById(addInfoRequest.getId())
            .orElseThrow(() -> new EntityNotFoundException("Add with ID " + addInfoRequest.getId() + " not found"));
        addDb.setAnimal_name(addInfoRequest.getAnimal_name());
        addDb.setAge(addInfoRequest.getAge());
        addDb.setBreed(addInfoRequest.getBreed());
        addDb.setGender(addInfoRequest.getGender());
        addDb.setDescription(addInfoRequest.getDescription());
        addDb.setCity(addInfoRequest.getCity());
        addDb.setDistrict(addInfoRequest.getDistrict());
        addDb.setPhone(addInfoRequest.getPhone());
        addDb.setEmail(addInfoRequest.getEmail());
        addRepository.save(addDb);
        return addDb;


    }


    private List<String> extractFolderAndFileName(String url) {
        String path = url.replaceFirst("https://[^/]+/", "");

        String[] parts = path.split("/");

        String folderName = parts[0];   
        String fileName = parts[1];     
        List<String> result = new ArrayList<>();
        result.add(folderName);
        result.add(fileName);

        return result;
    }


   

}


