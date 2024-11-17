package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import newREALs.backend.DTO.QuizStatusDto;
import newREALs.backend.service.QuizService;
import newREALs.backend.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts/profile")
public class ProfileController {
    private final QuizService quizService;
    private final TokenService tokenService;

    public ProfileController(QuizService quizService, TokenService tokenService) {
        this.quizService = quizService;
        this.tokenService = tokenService;
    }

    //[get] 프로필 페이지 나의 퀴즈 현황
    @GetMapping("/quiz")
    public ResponseEntity<List<QuizStatusDto>> getQuizStatus(HttpServletRequest userInfo) {
        Long userId = tokenService.getUserId(userInfo);

        List<QuizStatusDto> quizStatusList = quizService.getQuizStatus(userId);
        return ResponseEntity.ok(quizStatusList);
    }

}
