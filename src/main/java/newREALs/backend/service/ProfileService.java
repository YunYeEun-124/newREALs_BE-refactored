package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.DTO.ProfileInfoDTO;
import newREALs.backend.DTO.QuizStatusDTO;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.UserKeyword;
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

    public ProfileInfoDTO getProfilePage(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 유저 키워드 리스트에 저장하기
        List<UserKeyword> userKeywords = userKeywordRepository.findKeywordsByUserId(userId);
        List<String> keywordList = new ArrayList<>();
        for(UserKeyword userKeyword : userKeywords){
            keywordList.add(userKeyword.getKeyword().getName());
        }

        return ProfileInfoDTO.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .profilePath(account.getProfilePath())
                .point(account.getPoint())
                .keywords(keywordList)
                .build();
    }

    public QuizStatusDTO getQuizStatus(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));



    }
}
