package codeforces.codeSherpa.Controller;

import codeforces.codeSherpa.DTO.HintOutputDto;
import codeforces.codeSherpa.DTO.UrlInputDto;
import codeforces.codeSherpa.Service.CodeforcesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/api/problem")
@RestController
public class CodeforcesController {
    @Autowired
    private CodeforcesService service;


    @PostMapping("/getHints")
    public ResponseEntity<?> url(@Valid @RequestBody UrlInputDto url){
        try{
            HintOutputDto hint = service.getHint(url.getUrl());
            return ResponseEntity.ok(hint);
        }
        catch(Exception e){
            log.error("Error found in getHints endpoint", e);
            Map<String, Object> errorFormat = new HashMap<>();
            errorFormat.put("timestamp: ", LocalDateTime.now());
            errorFormat.put("HttpStatus :", 505);
            errorFormat.put("error :", "Internal Server Error");
            errorFormat.put("message :", e);
            return new ResponseEntity<>(errorFormat,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
