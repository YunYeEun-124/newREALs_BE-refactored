package newREALs.backend.news.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.common.dto.ApiResponseDTO;
import newREALs.backend.accounts.dto.LikesDTO;
import newREALs.backend.news.dto.NewsDetailDto;
import newREALs.backend.news.service.NewsDetailService;
import newREALs.backend.common.service.TokenService;
import newREALs.backend.accounts.service.UserActionService;
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

    @GetMapping("/likes/{id}")
    public ResponseEntity<ApiResponseDTO<?>> getReaction(
            @PathVariable Long id,
            HttpServletRequest request){
        Long userId=tokenService.getUserId(request);
        LikesDTO likesDTO=newsDetailService.getLikesDetail(id,userId);
        return ResponseEntity.ok(
                ApiResponseDTO.success("공감 정보를 성공적으로 조회했습니다.",likesDTO)
        );
    }



    //[post] 스크랩 버튼 클릭
    @PostMapping("/scrap/{id}")
    public ResponseEntity<ApiResponseDTO<Boolean>> toggleScrap(@PathVariable Long id, HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        boolean scrapped=userActionService.getScrap(id, userId);
        String message;
        if(scrapped)message="스크랩 등록 완료";
        else message="스크랩이 해제되었습니다.";
        return ResponseEntity.ok(
                ApiResponseDTO.success(message,scrapped));
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
