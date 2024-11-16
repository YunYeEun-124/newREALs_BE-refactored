package newREALs.backend.controller;

import newREALs.backend.dto.NewsDetailDto;
import newREALs.backend.service.NewsDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsDetailController {
    private final NewsDetailService newsDetailService;

    public NewsDetailController(NewsDetailService newsDetailService){
        this.newsDetailService=newsDetailService;
    }

    //[get]뉴스 상세 페이지
    @GetMapping("/{id}")
    public ResponseEntity<NewsDetailDto> getNewsDetail(@PathVariable Long id, @RequestParam Long userId){
        NewsDetailDto newsDetailDto=newsDetailService.getNewsDetail(id,userId);
        return ResponseEntity.ok(newsDetailDto);
    }

    //[post]스크랩 버튼 클릭
    @PostMapping("/scrap/{id}")
    public ResponseEntity<String> getScrap(@PathVariable Long id, @RequestParam Long userId){
        newsDetailService.getScrap(id,userId);
        return ResponseEntity.ok("스크랩 등록/삭제 완료");
    }

    //[post]공감 버튼 클릭
    @PostMapping("/likes/{id}")
    public ResponseEntity<String> getLikes(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam int reactionType){
        newsDetailService.getLikes(id, userId, reactionType);
        return ResponseEntity.ok("공감수 반영, 관심도 업데이트 성공");
    }

}
