package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.repository.AccountsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

// 출석 매달 초기화

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AccountsRepository accountsRepository;

    @Scheduled(cron  = "0 0 6 1 * *")
    @Transactional
    public void resetAttendanceList() {
        List<Accounts> accounts = accountsRepository.findAll();
        for (Accounts account : accounts) {
            Arrays.fill(account.getAttendanceList(), false); // AttendanceList 초기화
            accountsRepository.save(account);
        }
    }
}
