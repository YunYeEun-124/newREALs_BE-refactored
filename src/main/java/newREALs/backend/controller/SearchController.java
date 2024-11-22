package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.dto.SearchDTO;
import newREALs.backend.service.NewsService;
import newREALs.backend.service.NewsService2;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

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


        Long userid = tokenService.getUserId(userInfo);
        if(searchWord.isEmpty()){
            throw new IllegalArgumentException("매개변수 null");
        }

        SearchDTO result = newsService.getSearch(userid,searchWord,page);

        if(result.getTotalElement() == 0) {
            return ResponseEntity.ok(
                    ApiResponseDTO.success( "검색 결과가 없습니다.", result)
            );
        }

        if(page > result.getTotalPage() || page <= 0) {
            throw new IllegalArgumentException("페이지 범위 오류");
        }

        return ResponseEntity.ok(
                ApiResponseDTO.success( "검색 성공", result)
        );
    }
}