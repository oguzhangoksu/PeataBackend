package peata.backend.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import peata.backend.service.concretes.BannedIpService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


@Component
public class RateLimitFilter implements Filter {

    private static final long LOG_INTERVAL_MILLIS = Duration.ofMinutes(10).toMillis();
    private final Cache<String, Long> lastLogTimes = CacheBuilder.newBuilder()
    .expireAfterWrite(12, TimeUnit.HOURS)  
    .maximumSize(100_000)                   
    .build();

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired
    private BannedIpService bannedIpService;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ipAddress = getClientIP(httpRequest);
        if (shouldLogIp(ipAddress)) {
            logger.info("Request IP: {}", ipAddress);
        }

        if (bannedIpService.isBanned(ipAddress)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("Your IP is banned.");
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Rate limit exceeded. Try again later.");
            System.out.println("Rate limit exceeded for IP: " + ipAddress);
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }
    private boolean shouldLogIp(String ip) {
        long now = System.currentTimeMillis();
        Long lastLogTime = lastLogTimes.getIfPresent(ip);
    
        if (lastLogTime == null || (now - lastLogTime) >= LOG_INTERVAL_MILLIS) {
            lastLogTimes.put(ip, now); 
            return true;
        }
        return false;
    }
}