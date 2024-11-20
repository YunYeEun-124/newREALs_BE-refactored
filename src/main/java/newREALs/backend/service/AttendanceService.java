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
    public boolean UpdateAttendance(Long userid) {

        Optional<Accounts> user = userRepository.findById(userid);
        LocalDateTime current = LocalDateTime.now();
        int hour = current.getHour();
        int day = current.getDayOfMonth();

        if(user.isPresent()){

            if (hour < 6) day -= 2; //새벽 6시 이전 : 00:00~05:59에 들어온다. -> 전날
            else day --;
            if(!user.get().getAttendanceList()[day]) {
                user.get().updateAttendance(day);
                return true;
            }

        }

        return false;
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