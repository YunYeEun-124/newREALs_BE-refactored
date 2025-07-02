package newREALs.backend.news.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.common.dto.ApiResponseDTO;
import newREALs.backend.news.dto.QuizDto;
import newREALs.backend.news.service.QuizService;
import newREALs.backend.common.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/quiz")
public class QuizController {
    private final QuizService quizService;
    private final TokenService tokenService;


    // 퀴즈 풀기
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Boolean>> solveQuiz(
            @PathVariable Long id,
            @RequestParam Boolean userAnswer,
            HttpServletRequest request) {

        Long userId = tokenService.getUserId(request);
        Boolean result=quizService.solveQuiz(id,userId,userAnswer);
        quizService.checkExtraPoint(userId);

        String message = result ? "정답입니다!" : "오답입니다.";
        return ResponseEntity.ok(ApiResponseDTO.success(message, result));
    }

    // 뉴스 상세페이지 - 퀴즈
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<QuizDto>> getQuiz(@PathVariable Long id, HttpServletRequest request) {
        Long userId = tokenService.getUserId(request);
        QuizDto quizDto = quizService.getQuiz(id, userId);

        if (quizDto == null) {
            //메인 뉴스가 아닌 일반 뉴스라 퀴즈가 없음. 응답은 200 OK로 주고 data만 null로 전달
            return ResponseEntity.ok(ApiResponseDTO.success("일반 뉴스에는 퀴즈가 없습니다.", null));
        }

        return ResponseEntity.ok(ApiResponseDTO.success("퀴즈 조회 성공", quizDto));
    }
}
