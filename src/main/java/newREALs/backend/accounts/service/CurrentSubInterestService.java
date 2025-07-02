package newREALs.backend.accounts.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.accounts.domain.PreviousSubInterest;
import newREALs.backend.accounts.domain.CurrentSubInterest;
import newREALs.backend.accounts.repository.PreviousSubInterestRepository;
import newREALs.backend.accounts.repository.CurrentSubInterestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrentSubInterestService {
    private final CurrentSubInterestRepository currentSubInterestRepository;
    private final PreviousSubInterestRepository previousSubInterestRepository;

    // subInterest -> preSubInterest
    @Scheduled(cron = "0 06 11 29 * ?")
    @Transactional
    public void SubInterestToPreSubInterest() {
        previousSubInterestRepository.deleteAll();

        List<CurrentSubInterest> currentSubInterests = currentSubInterestRepository.findAll();
        for (CurrentSubInterest currentSubInterest : currentSubInterests) {
            PreviousSubInterest previousSubInterest = PreviousSubInterest.builder()
                    .commentCount(currentSubInterest.getCommentCount())
                    .count(currentSubInterest.getCount())
                    .quizCount(currentSubInterest.getQuizCount())
                    .scrapCount(currentSubInterest.getScrapCount())
                    .attCount(currentSubInterest.getAttCount())
                    .subCategory(currentSubInterest.getSubCategory())
                    .user(currentSubInterest.getUser())
                    .build();
            previousSubInterestRepository.save(previousSubInterest);
        }
    }

    // subInterst 초기화
    @Scheduled(cron = "0 00 11 29 * ?")
    @Transactional
    public void resetSubInterest() {
        List<CurrentSubInterest> currentSubInterests = currentSubInterestRepository.findAll();
        for (CurrentSubInterest currentSubInterest : currentSubInterests) {
            currentSubInterest.setCount(0);
            currentSubInterest.setCommentCount(0);
            currentSubInterest.setQuizCount(0);
            currentSubInterest.setAttCount(0);
            currentSubInterest.setScrapCount(0);
            currentSubInterest.setAttCount(0);
        }
    }


}
