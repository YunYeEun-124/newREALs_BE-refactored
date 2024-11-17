package newREALs.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import newREALs.backend.service.PopUpService;
import newREALs.backend.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/popup/")
public class PopUpController {
    private final PopUpService popUpService;
    private final TokenService tokenService; // TokenService 추가

    public PopUpController(PopUpService popUpService, TokenService tokenService) {
        this.popUpService = popUpService;
        this.tokenService = tokenService;
    }

    // 출석 완료 버튼 클릭
    @PostMapping("/attendance/mark")
    public ResponseEntity<String> getAttendance(HttpServletRequest userInfo) {
        Long userId = tokenService.getUserId(userInfo);
        return popUpService.markAttendance(userId);
    }

}
