package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.SearchDTO;
import newREALs.backend.service.NewsService;
import newREALs.backend.service.NewsService2;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final NewsService2 newsService;
    private final TokenService tokenService;


    @GetMapping
    public ResponseEntity<?> getNewsList(HttpServletRequest userInfo, @RequestParam String searchWord, @RequestParam int page){

        Long userid = tokenService.getUserId(userInfo);
        //./ Long userid = 1l;
        if(searchWord.isEmpty()){
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", 400);
            errorResponse.put("error", "searchWord is empty");
            errorResponse.put("path", "/search");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        SearchDTO result = newsService.getSearch(userid,searchWord,page);

        return  ResponseEntity.ok().body(result);
    }
}