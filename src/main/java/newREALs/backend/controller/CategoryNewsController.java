package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.ViewCategoryDTO;
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
@RequestMapping("/category")
public class CategoryNewsController {

    //service : 주요뉴스 1개, basenews 리스트,
    private final NewsService2 newsService;
    private  TokenService tokenService;

    // category/sub/?category=society&subcategory=사건사고
    @GetMapping("/sub")
    public ResponseEntity<?> viewSubCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam String subCategory,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);

        //page오바하면
        ViewCategoryDTO result  = newsService.getSubCategory(userid,category,subCategory,page);

        return ResponseEntity.ok().body(result);
    }

    // category/?categoryname=society
    @GetMapping
    public ResponseEntity<?> viewCategory(HttpServletRequest userInfo, @RequestParam String category,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);
        //page오바하면
        ViewCategoryDTO result  = newsService.getCategory(userid,category,page);
        if(result.getBasenewsList().isEmpty()){
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", 404);
            errorResponse.put("error", "Not News Here");
            errorResponse.put("path", "/catgory");
            errorResponse.put("totalPage",result.getTotalPage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok().body(result);
    }

}