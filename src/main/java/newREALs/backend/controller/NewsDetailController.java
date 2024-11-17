package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import newREALs.backend.DTO.NewsDetailDto;
import newREALs.backend.service.NewsDetailService;
import newREALs.backend.service.TokenService;
import newREALs.backend.service.UserActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsDetailController {
    private final NewsDetailService newsDetailService;
    private final UserActionService userActionService;
    private final TokenService tokenService;

    public NewsDetailController(NewsDetailService newsDetailService, UserActionService userActionService, TokenService tokenService) {
        this.newsDetailService = newsDetailService;
        this.userActionService = userActionService;
        this.tokenService = tokenService;
    }

    //[get] 뉴스 상세 페이지
    @GetMapping("/{id}")
    public ResponseEntity<NewsDetailDto> getNewsDetail(
            @PathVariable Long id,
            HttpServletRequest userInfo,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String keyword) {

        Long userId = tokenService.getUserId(userInfo);
        NewsDetailDto newsDetail = newsDetailService.getNewsDetail(id, userId, category, subCategory, keyword);
        return ResponseEntity.ok(newsDetail);
    }

    //[post] 스크랩 버튼 클릭
    @PostMapping("/scrap/{id}")
    public ResponseEntity<String> toggleScrap(@PathVariable Long id, HttpServletRequest userInfo) {
        Long userId = tokenService.getUserId(userInfo);
        userActionService.getScrap(id, userId);
        return ResponseEntity.ok("스크랩 등록/삭제 완료");
    }

    //[post] 공감 버튼 클릭
    @PostMapping("/likes/{id}")
    public ResponseEntity<String> getLikes(
            @PathVariable Long id,
            HttpServletRequest userInfo,
            @RequestParam int reactionType) {
        Long userId = tokenService.getUserId(userInfo);
        userActionService.getLikes(id, userId, reactionType);
        return ResponseEntity.ok("공감수 반영, 관심도 업데이트 성공");
    }
//    //[get]뉴스 상세 페이지
//    @GetMapping("/{id}")
//    public ResponseEntity<NewsDetailDto> getNewsDetail(
//            @PathVariable Long id,
//            @RequestParam Long userId,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String subCategory,
//            @RequestParam(required=false) String keyword){
//        NewsDetailDto newsDetail = newsDetailService.getNewsDetail(id, userId, category, subCategory,keyword);
//        return ResponseEntity.ok(newsDetail);
//    }
//
//    //[post]스크랩 버튼 클릭
//    @PostMapping("/scrap/{id}")
//    public ResponseEntity<String> toggleScrap(@PathVariable Long id, @RequestParam Long userId){
//        userActionService.getScrap(id,userId);
//        return ResponseEntity.ok("스크랩 등록/삭제 완료");
//    }
//
//    //[post]공감 버튼 클릭
//    @PostMapping("/likes/{id}")
//    public ResponseEntity<String> getLikes(
//            @PathVariable Long id,
//            @RequestParam Long userId,
//            @RequestParam int reactionType){
//        userActionService.getLikes(id, userId, reactionType);
//        return ResponseEntity.ok("공감수 반영, 관심도 업데이트 성공");
//    }

}
