package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import newREALs.backend.dto.QuizDto;
import newREALs.backend.service.QuizService;
import newREALs.backend.service.TokenService;
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
    public ResponseEntity<Boolean> solveQuiz(
            @PathVariable Long id,
            @RequestParam Boolean userAnswer,
            HttpServletRequest userInfo) {

        Long userId = tokenService.getUserId(userInfo);
        Boolean result=quizService.solveQuiz(id,userId,userAnswer);
        quizService.checkExtraPoint(userId);
        return ResponseEntity.ok(result);
    }

    // 뉴스 상세페이지 - 퀴즈
    @GetMapping("/{id}")
    public ResponseEntity<QuizDto> getQuiz(@PathVariable Long id, HttpServletRequest userInfo) {
        Long userId = tokenService.getUserId(userInfo);
        QuizDto quizDto = quizService.getQuiz(id, userId);
        return ResponseEntity.ok(quizDto);
    }
}
