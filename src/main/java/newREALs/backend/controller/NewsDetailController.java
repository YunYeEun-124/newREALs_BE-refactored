package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.dto.NewsDetailDto;
import newREALs.backend.service.NewsDetailService;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsDetailController {
    private final NewsDetailService newsDetailService;
    private final UserActionService userActionService;
    private final TokenService tokenService;

    //[get] 뉴스 상세 페이지
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NewsDetailDto>> getNewsDetail(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String keyword) {

        Long userId = tokenService.getUserId(request); // 인증 정보에서 사용자 ID 추출
        NewsDetailDto newsDetail = newsDetailService.getNewsDetail(id, userId, category, subCategory, keyword);

        return ResponseEntity.ok(
                ApiResponseDTO.success( "뉴스 상세 정보를 성공적으로 조회했습니다.", newsDetail)
        );
    }

    //[post] 스크랩 버튼 클릭
    @PostMapping("/scrap/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> toggleScrap(@PathVariable Long id, HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        String message=userActionService.getScrap(id, userId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(message,null));
    }

    //[post] 공감 버튼 클릭
    @PostMapping("/likes/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> getLikes(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam int reactionType) {
        Long userId = tokenService.getUserId(request);
        String message=userActionService.getLikes(id, userId, reactionType);
        return ResponseEntity.ok(
                ApiResponseDTO.success(message,null));
    }


}
