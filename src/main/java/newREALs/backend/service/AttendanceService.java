package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// 출석 매달 초기화

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final UserRepository userRepository;

    public boolean GetAttendance(Long userid){
        Optional<Accounts> user = userRepository.findById(userid);
        LocalDateTime current = LocalDateTime.now();
        int day = current.getDayOfMonth();

        if(user.isPresent()){
            day --;
            return user.get().getAttendanceList()[day];  //출석체크함.

        } else {
            return false;
        }

    }


    @Transactional
    public int UpdateAttendance(Long userid) {

        Optional<Accounts> user = userRepository.findById(userid);
        LocalDateTime current = LocalDateTime.now();

        int day = current.getDayOfMonth()-1;

        if(user.isPresent()){

            if(!user.get().getAttendanceList()[day]) {
                user.get().updateAttendance(day);
                return day;
            }else{
                System.out.println("이미 출석체크함. ");
                return day;
            }

        }

        return -1;
    }

    @Scheduled(cron  = "0 0 6 1 * *")
    @Transactional
    public void resetAttendanceList() {
        List<Accounts> accounts = userRepository.findAll();
        for (Accounts account : accounts) {
            Arrays.fill(account.getAttendanceList(), false); // AttendanceList 초기화
            userRepository.save(account);
        }
    }
}