package peata.backend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static ResponseEntity<Map<String, Object>> success(String message) {
        return success(message, null);
    }

    public static ResponseEntity<Map<String, Object>> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> error(String message) {
        return error(message, null, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Map<String, Object>> error(String message, Object data) {
        return error(message, data, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Map<String, Object>> error(String message, Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.status(status).body(response);
    }
}
