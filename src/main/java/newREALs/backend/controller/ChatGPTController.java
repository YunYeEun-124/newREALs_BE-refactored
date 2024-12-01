package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Basenews;
import newREALs.backend.dto.ApiResponseDTO;
import newREALs.backend.dto.GPTbasenewsDTO;
import newREALs.backend.repository.BaseNewsRepository;
import newREALs.backend.service.ChatGPTService;
import newREALs.backend.service.NewsService;
import newREALs.backend.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gpt")
public class ChatGPTController {
    private final ChatGPTService chatGPTService;
    private final NewsService newsService;
    private final TokenService tokenService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<?>> getGptProcessedArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        try {
            // processArticle 메서드 실행
            Basenews processedNews = newsService.processArticle(id);
            // DTO로 변환
            GPTbasenewsDTO gpTbasenewsDTO=new GPTbasenewsDTO(
                    processedNews.getTitle(),
                    processedNews.getDescription(),
                    processedNews.getSummary(),
                    processedNews.getTermList().stream()
                            .map(term->new GPTbasenewsDTO.TermDto(term.getTerm(),term.getTermInfo()))
                            .collect(Collectors.toList())
            );
            // 성공 응답
            return ResponseEntity.ok(
                    ApiResponseDTO.success("GPT를 활용한 뉴스 요약, 설명, 용어 생성 성공", gpTbasenewsDTO)
            );
        } catch (IllegalArgumentException e) {
            // 잘못된 뉴스 ID 처리
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.failure("E400", "잘못된 뉴스 ID입니다: " + e.getMessage()));
        } catch (Throwable e) {
            // 기타 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.failure("E500", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }


}
