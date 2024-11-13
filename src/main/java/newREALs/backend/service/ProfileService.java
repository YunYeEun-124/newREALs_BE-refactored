package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.DTO.ProfileAttendanceListDTO;
import newREALs.backend.DTO.ProfileInfoDTO;
import newREALs.backend.DTO.ProfileQuizStatusDTO;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Dailynews;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.repository.AccountsRepository;
import newREALs.backend.repository.DailyNewsRepository;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final DailyNewsRepository dailyNewsRepository;
    private final AccountsRepository accountsRepository;

    public ProfileInfoDTO getProfileInfo(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 유저 키워드 리스트에 저장하기
        List<Keyword> userKeywords = userKeywordRepository.findKeywordsById(userId);
        List<String> keywordList = new ArrayList<>();
        for(Keyword userKeyword : userKeywords){
            keywordList.add(userKeyword.getName());
        }

        return ProfileInfoDTO.builder()
                .userId(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .profilePath(account.getProfilePath())
                .point(account.getPoint())
                .keywords(keywordList)
                .build();
    }

    public ProfileQuizStatusDTO getQuizStatus(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Dailynews dailyNews = dailyNewsRepository.findByUserId(account.getId());

        return ProfileQuizStatusDTO.builder()
                .userId(account.getId())
                .quizList(dailyNews.getQuizList())
                .quizStatus(dailyNews.getQuizStatus())
                .build();
    }

    public ProfileAttendanceListDTO getAttendanceList(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        List<Boolean> attendanceList = accountsRepository.findAttendanceListByUserId(userId);

        return ProfileAttendanceListDTO.builder()
                .userId(account.getId())
                .attendanceList(attendanceList)
                .build();
    }
}
