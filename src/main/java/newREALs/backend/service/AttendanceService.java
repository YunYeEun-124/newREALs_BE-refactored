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

    @Transactional
    public int UpdateAttendance(Long userid) {

        Optional<Accounts> user = userRepository.findById(userid);
        LocalDateTime current = LocalDateTime.now();
        int hour = current.getHour();
        int day = current.getDayOfMonth();

        if(user.isPresent()){
            //마지막날일때 주의 해야하는데 ...
            if (hour < 6) day = current.minusDays(1).getDayOfMonth() -1; //새벽 6시 이전 : 00:00~05:59에 들어온다. -> 전날
            else day --;

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