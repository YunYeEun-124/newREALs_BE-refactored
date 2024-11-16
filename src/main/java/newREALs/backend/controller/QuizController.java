package newREALs.backend.controller;

import newREALs.backend.dto.QuizDto;
import newREALs.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news/quiz")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }
    //퀴즈 풀기
    @PostMapping("/{id}")
    public ResponseEntity<Boolean> solveQuiz(@PathVariable Long id, @RequestParam Long userId, @RequestParam Boolean userAnswer){
        return ResponseEntity.ok(quizService.solveQuiz(id,userId,userAnswer));
    }

    //뉴스 상세페이지 - 퀴즈
    @GetMapping("/{id}")
    public ResponseEntity<QuizDto> getQuiz(@PathVariable Long id, @RequestParam Long userId){
        QuizDto quizDto=quizService.getQuiz(id,userId);
        return ResponseEntity.ok(quizDto);
    }
}
