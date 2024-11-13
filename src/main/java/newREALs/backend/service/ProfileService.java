package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.DTO.ProfileAttendanceListDTO;
import newREALs.backend.DTO.ProfileInfoDTO;
import newREALs.backend.DTO.ProfileQuizStatusDTO;
import newREALs.backend.DTO.QuizDTO;
import newREALs.backend.domain.*;
import newREALs.backend.repository.AccountsRepository;
import newREALs.backend.repository.DailyNewsRepository;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                .user_id(account.getId())
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

        List<Quiz> quizList = accountsRepository.findQuizListByUserId(userId);
        List<QuizDTO> quizDTOList = new ArrayList<>();
        for (Quiz quiz : quizList) {
            quizDTOList.add(new QuizDTO(quiz));
        }

        List<Integer> quizStatus = accountsRepository.findQuizStatusByUserId(userId);

        return ProfileQuizStatusDTO.builder()
                .user_id(account.getId())
                .quizList(quizDTOList)
                .quizStatus(quizStatus)
                .build();
    }

    public ProfileAttendanceListDTO getAttendanceList(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        List<Boolean> attendanceList = accountsRepository.findAttendanceListByUserId(userId);

        return ProfileAttendanceListDTO.builder()
                .user_id(account.getId())
                .attendanceList(attendanceList)
                .build();
    }
}
