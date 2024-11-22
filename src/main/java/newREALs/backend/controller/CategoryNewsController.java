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
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryNewsController {

    //service : 주요뉴스 1개, basenews 리스트,
    private final NewsService2 newsService;
    private final TokenService tokenService;

    // category/sub?category=society&subcategory=사건사고
    @GetMapping("/sub")
    public ResponseEntity<?> viewSubCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam String subCategory,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);

        //page오바하면
        ViewCategoryDTO result  = newsService.getSubCategory(userid,category,subCategory,page);

        return ResponseEntity.ok().body(result);
    }

    // category?categoryname=society
    @GetMapping
    public ResponseEntity<?> viewCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam int page) {
        Long userid = tokenService.getUserId(userInfo);
        ViewCategoryDTO result = newsService.getCategory(userid, category, page);

        // 카테고리 입력 확인
        if (category == null || category.isEmpty()) {
            return createResponse(false, "E400", "카테고리를 입력해주세요", null, HttpStatus.BAD_REQUEST);
        }

        // 페이지 범위 확인
        if (page < 1 || page > result.getTotalPage()) {
            return createResponse(false, "E400", "Page index 초과 또는 유효하지 않은 페이지 요청", null, HttpStatus.BAD_REQUEST);
        }

        // 결과가 비어 있는 경우
        if (result.getBasenewsList().isEmpty()) {
            return createResponse(false, "E400", category + " 카테고리에 해당하는 뉴스가 없습니다.", null, HttpStatus.NOT_FOUND);
        }

        // 성공 응답
        return createResponse(true, "S200", category + " 페이지 조회 성공", result, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> createResponse(boolean isSuccess, String code, String message, Object data, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", isSuccess);
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.status(status).body(response);
    }

}