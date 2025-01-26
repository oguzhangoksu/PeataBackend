package peata.backend.service.concretes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BannedIpService {
    private volatile List<String> bannedIps = Collections.emptyList();

    @Scheduled(fixedRate = 60000) // 60 saniyede bir örnek
    public void refreshBannedIps() {
        this.bannedIps = loadIpListFromFile();
    }

    public boolean isBanned(String ip) {
        return bannedIps.contains(ip);
    }

    private List<String> loadIpListFromFile() {
        try {
            return Files.readAllLines(Paths.get("bannedList", "banned_ips.txt"))
                .stream()
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}