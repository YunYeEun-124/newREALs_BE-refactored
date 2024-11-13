package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Dailynews;
import newREALs.backend.repository.DailyNewsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizStatusResetService {

    private final DailyNewsRepository dailynewsRepository;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void resetQuizStatus() {
        List<Dailynews> dailynewsList = dailynewsRepository.findAll();

        for (Dailynews dailynews : dailynewsList) {
            List<Integer> quizStatus = dailynews.getQuizStatus();
            for (int i = 0; i < 5; i++) { // 퀴즈 상태 원소 항상 5개
                quizStatus.set(i, 0);
            }
        }
        dailynewsRepository.saveAll(dailynewsList);
    }
}
