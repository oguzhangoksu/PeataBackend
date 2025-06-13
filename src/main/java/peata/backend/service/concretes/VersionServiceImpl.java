package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Version;
import peata.backend.repositories.VersionRepository;
import peata.backend.service.abstracts.VersionService;

@Service
public class VersionServiceImpl implements VersionService{
    
    @Autowired
    private VersionRepository versionRepository;

    public boolean isValiadteVersion(String version) {
        Version latestVersion = versionRepository.findByVersion(version);
        if(latestVersion != null ) {
            return true;
        }
        return false;
    }


}
