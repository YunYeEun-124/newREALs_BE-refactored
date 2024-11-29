package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.PreSubInterest;
import newREALs.backend.domain.SubInterest;
import newREALs.backend.repository.PreSubInterestRepository;
import newREALs.backend.repository.SubInterestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubInterestService {
    private final SubInterestRepository subInterestRepository;
    private final PreSubInterestRepository preSubInterestRepository;

    // subInterest -> preSubInterest
    @Scheduled(cron = "0 06 11 29 * ?")
    @Transactional
    public void SubInterestToPreSubInterest() {
        preSubInterestRepository.deleteAll();

        List<SubInterest> subInterests = subInterestRepository.findAll();
        for (SubInterest subInterest : subInterests) {
            PreSubInterest preSubInterest = PreSubInterest.builder()
                    .commentCount(subInterest.getCommentCount())
                    .count(subInterest.getCount())
                    .quizCount(subInterest.getQuizCount())
                    .scrapCount(subInterest.getScrapCount())
                    .attCount(subInterest.getAttCount())
                    .subCategory(subInterest.getSubCategory())
                    .user(subInterest.getUser())
                    .build();
            preSubInterestRepository.save(preSubInterest);
        }
    }

    // subInterst 초기화
    @Scheduled(cron = "0 00 11 29 * ?")
    @Transactional
    public void resetSubInterest() {
        List<SubInterest> subInterests = subInterestRepository.findAll();
        for (SubInterest subInterest : subInterests) {
            subInterest.setCount(0);
            subInterest.setCommentCount(0);
            subInterest.setQuizCount(0);
            subInterest.setAttCount(0);
            subInterest.setScrapCount(0);
            subInterest.setAttCount(0);
        }
    }


}
