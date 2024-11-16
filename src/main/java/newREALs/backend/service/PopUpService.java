package newREALs.backend.service;

import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PopUpService {
    private final UserRepository userRepository;

    public PopUpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //출석 완료 처리 메서드
    public ResponseEntity<String> markAttendance(Long userId) {
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Invalid user ID"));

        int date= LocalDate.now().getDayOfMonth()-1;
//        if(user.getAttendanceList().get(date)){ //if(user.getAttendanceList()[date])
//            return ResponseEntity.badRequest().body("이미 출석을 완료했어요.");
//        }
        if(user.getAttendanceList()[date]){
                return ResponseEntity.badRequest().body("이미 출석을 완료했어요.");
        }

        //user.getAttendanceList().set(date,true);
        user.getAttendanceList()[date]=true;
        user.setPoint(user.getPoint()+150);
        userRepository.save(user);
        return ResponseEntity.ok("출석 완료. 150포인트 적립");

    }


}
