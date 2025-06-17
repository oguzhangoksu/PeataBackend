package peata.backend.service.abstracts;


import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.entity.User;
import peata.backend.utils.FileData;
import peata.backend.utils.Requests.AddRequest;
import peata.backend.utils.Requests.UpdateAddInfoRequest;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface AddService {

    
    public void delete(Long id);
    public Add save(AddRequest addRequest,List<FileData> fileDatas) throws IOException;
    public AddDto findAddDtoById(Long id);
    public Add findAddById(Long id);
    public Add findAddByIdWithOutDto(Long id);
    public List<Add> allAdds();
    public List<String> findImagesByAddId(Long id);
    public Page<AddDto> getPaginatedAdds(int page, int size);
    public Page<AddDto> getPaginatedAddswithStatus(int status,int page, int size);
    public Add save(Add add,User user);
    public boolean existsById(Long id);
    public void deleteImage(AddDto addDto, List<String> imageName);
    public List<String> addImage(AddDto addDto,List<MultipartFile> files)throws IOException;
    public Add updateAddDto(UpdateAddInfoRequest addInfoRequest);
    public AddDto findAddByPcode(String pCode);
    public List<AddDto> findAddsbyCountryId(int countryId);
    public Page<AddDto> getPaginatedAddswithCountryId(int countryId,int page, int size);


}
