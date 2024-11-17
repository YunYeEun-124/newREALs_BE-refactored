package newREALs.backend.controller;

import newREALs.backend.service.PopUpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/popup/")
public class PopUpController {
    private final PopUpService popUpService;

    public PopUpController(PopUpService popUpService){
        this.popUpService=popUpService;
    }

    //출석 완료 버튼 클릭
    @PostMapping("/attendance/mark")
    public ResponseEntity<String> getAttendance(@RequestParam Long userId){
        return popUpService.markAttendance(userId);
    }


}
