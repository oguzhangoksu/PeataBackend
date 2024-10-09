package peata.backend.service.abstracts;

import java.io.IOException;
import java.util.List;

import peata.backend.utils.FileData;

public interface S3Service {
    public List<String> uploadFilesToFolder(String folderName, List<FileData> files)throws IOException;
    public void deleteFile(String filename);
    public void deleteFolder(String folderName);
}   
 