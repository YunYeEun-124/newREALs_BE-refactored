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
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final NewsService2 newsService;
    private final TokenService tokenService;


    @GetMapping
    public ResponseEntity<?> getNewsList(HttpServletRequest userInfo, @RequestParam String searchWord, @RequestParam int page){

        System.out.println("search : "+searchWord);
        System.out.println("page : "+page);


        Long userid = tokenService.getUserId(userInfo);
        Map<String, Object> response = new LinkedHashMap<>();
        if(searchWord.isEmpty()){
            response.put("isSuccess", false);
            response.put("code", "E400");
            response.put("message", "검색어를 입력해주세요 ");
            response.put("data",null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        SearchDTO result = newsService.getSearch(userid,searchWord,page);

        if(result.getTotalElement() == 0) {
            response.put("isSuccess", false);
            response.put("code", "E400");
            response.put("message", "해당 검색어에 대한 결과가 없습니다.");
            response.put("data",null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if(page > result.getTotalPage()) {
            response.put("isSuccess", false);
            response.put("code", "E400");
            response.put("message", "페이지 범위를 초과했습니다. ");
            response.put("data",null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("isSuccess", true);
        response.put("code", "S200");
        response.put("message", "검색 성공");
        response.put("data", result);


        return   ResponseEntity.status(HttpStatus.OK).body(response);
    }
}