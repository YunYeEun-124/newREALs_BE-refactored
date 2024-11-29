package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.dto.ApiResponseDTO;
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
        if (category.isEmpty()||subCategory.isEmpty())
             throw new IllegalArgumentException("매개변수 null");

        if(!category.equals("사회") && !category.equals("경제") && !category.equals("정치") ){
            throw  new IllegalArgumentException("잘못된 카테고리 입력");
        }

        Optional<SubCategory> sub = subCategoryRepository.findByName(subCategory);

        if (sub.isPresent() && category.equals(sub.get().getCategory().getName())){ //subcategory - category mapping check
             ViewCategoryDTO result  = newsService.getSubCategory(userid,category,subCategory,page);
            // 결과가 비어 있는 경우
            if (result.getBasenewsList().isEmpty() || result.getDailynews() == null)
                return ResponseEntity.ok(
                        ApiResponseDTO.success( "해당 뉴스가 존재하지 않습니다.", result)
                );
            // 페이지 범위 확인
            if (page <= 0 ||page > result.getTotalPage())
                throw  new IllegalArgumentException("페이지 범위 오류");
            return ResponseEntity.ok(
                    ApiResponseDTO.success( "소 카테고리 뉴스 조회 성공.", result)
            );

        }else
            throw  new IllegalArgumentException("잘못된 소카테고리-카테고리 mapping 입니다.");



    }

    // category?category=society
    @GetMapping
    public ResponseEntity<?> viewCategory(HttpServletRequest userInfo, @RequestParam String category, @RequestParam int page) {
        Long userid = tokenService.getUserId(userInfo);

        // 카테고리 입력 확인
        if (category == null || category.isEmpty()) {
            throw  new IllegalArgumentException("매개변수 null");
        }
        if(!category.equals("사회") && !category.equals("경제") && !category.equals("정치") ){
            throw  new IllegalArgumentException("잘못된 카테고리 입력");
        }
        ViewCategoryDTO result = newsService.getCategory(userid, category, page);

        // 결과가 비어 있는 경우
        if (result.getBasenewsList().isEmpty() || result.getDailynews() == null) {
            throw  new IllegalStateException("데이터 불러오기 실패");
        }
        // 페이지 범위 확인
        if (page < 1 || page > result.getTotalPage()) {
            throw  new IllegalArgumentException("페이지 범위 오류");
        }
        // 성공 응답
        return ResponseEntity.ok(
                ApiResponseDTO.success( "카테고리 뉴스 조회 성공", result)
        );
    }



}