package peata.backend.service.abstracts;


import peata.backend.dtos.AddDto;
import peata.backend.entity.Add;
import peata.backend.utils.FileData;
import peata.backend.utils.Requests.AddRequest;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;

public interface AddService {

    
    public void delete(Long id);
    public Add save(AddRequest addRequest,List<FileData> fileDatas) throws IOException;
    public AddDto findAddById(Long id);
    public List<Add> allAdds();
    public List<String> findImagesByAddId(Long id);
    public Page<AddDto> getPaginatedAdds(int page, int size);
}
