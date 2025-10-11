package codeforces.codeSherpa.Controller;

import codeforces.codeSherpa.DTO.ChatInputDto;
import codeforces.codeSherpa.DTO.ChatOutputDto;
import codeforces.codeSherpa.DTO.HintOutputDto;
import codeforces.codeSherpa.DTO.UrlInputDto;
import codeforces.codeSherpa.Service.CodeforcesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/api/problem")
@RestController
@CrossOrigin(origins = "http://localhost:8080") // Adjust if your frontend runs on a different port
public class CodeforcesController {

    @Autowired
    private CodeforcesService service;

    @PostMapping("/getHints")
    public ResponseEntity<?> getHints(@Valid @RequestBody UrlInputDto url) {
        try {
            HintOutputDto hint = service.getHint(url.getUrl());
            return ResponseEntity.ok(hint);
        } catch (Exception e) {
            log.error("Error in /getHints endpoint", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatInputDto chatInput) {
        try {
            // Pass the entire DTO to the service
            String reply = service.getChatReply(chatInput);
            return ResponseEntity.ok(new ChatOutputDto(reply));
        } catch (Exception e) {
            log.error("Error in /chat endpoint", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
