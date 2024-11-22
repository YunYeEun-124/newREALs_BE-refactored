package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.dto.ViewCategoryDTO;
import newREALs.backend.repository.SubCategoryRepository;
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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryNewsController {

    //service : 주요뉴스 1개, basenews 리스트,
    private final NewsService2 newsService;
    private final TokenService tokenService;
    private final SubCategoryRepository subCategoryRepository;

    // category/sub?category=society&subcategory=사건사고
    //categpru sub 매칭

    @GetMapping("/sub")
    public ResponseEntity<?> viewSubCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam String subCategory,@RequestParam int page){
        Long userid = tokenService.getUserId(userInfo);
        // 카테고리 입력 확인
        if (category.isEmpty()||subCategory.isEmpty()) {
            return createResponse(false, "E400", "카테고리를 입력해주세요", null, HttpStatus.BAD_REQUEST);
        }
        if(!category.equals("society") && !category.equals("politics") && !category.equals("economy") ){
            return createResponse(false, "E400", "잘못된 카테고리 입력입니다.", category, HttpStatus.BAD_REQUEST);
        }

        Optional<SubCategory> sub = subCategoryRepository.findByName(subCategory);

        if (sub.isPresent()&& category.equals(sub.get().getCategory().getName())){ //subcategory - category mapping check
             ViewCategoryDTO result  = newsService.getSubCategory(userid,category,subCategory,page);
            // 결과가 비어 있는 경우
            if (result.getBasenewsList().isEmpty() || result.getDailynews() == null) {
                return createResponse(false, "E500", "뉴스 불러오기 실패", null, HttpStatus.NOT_FOUND);
            }

            // 페이지 범위 확인
            if (page > result.getTotalPage()) {
                return createResponse(false, "E400", "Page index 초과 또는 유효하지 않은 페이지 요청", null, HttpStatus.BAD_REQUEST);
            }

            return createResponse(true, "S200", subCategory + " 페이지 조회 성공", result, HttpStatus.OK);
        }else{
            return createResponse(false, "E400", " 잘못된 소카테고리 입니다. ",subCategory, HttpStatus.BAD_REQUEST);
        }


    }

    // category?category=society
    @GetMapping
    public ResponseEntity<?> viewCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam int page) {
        Long userid = tokenService.getUserId(userInfo);

        // 카테고리 입력 확인
        if (category == null || category.isEmpty()) {
            return createResponse(false, "E400", "카테고리를 입력해주세요", null, HttpStatus.BAD_REQUEST);
        }
        if(!category.equals("society") && !category.equals("politics") && !category.equals("economy") ){
            System.out.println("category is wrong");
            return createResponse(false, "E400", "잘못된 카테고리 입력입니다.", category, HttpStatus.BAD_REQUEST);
        }
        ViewCategoryDTO result = newsService.getCategory(userid, category, page);

        // 결과가 비어 있는 경우
        if (result.getBasenewsList().isEmpty() || result.getDailynews() == null) {
            return createResponse(false, "E500", "뉴스 불러오기 실패", null, HttpStatus.NOT_FOUND);
        }
        // 페이지 범위 확인
        if (page < 1 || page > result.getTotalPage()) {
            return createResponse(false, "E400", "Page index 초과 또는 유효하지 않은 페이지 요청", null, HttpStatus.BAD_REQUEST);
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