package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.dto.*;
import newREALs.backend.domain.*;
import newREALs.backend.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final AccountsRepository accountsRepository;

    public ProfileInfoDto getProfileInfo(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 유저 키워드 리스트에 저장하기
        List<Keyword> userKeywords = userKeywordRepository.findKeywordsById(userId);
        List<String> keywordList = new ArrayList<>();
        for(Keyword userKeyword : userKeywords){
            keywordList.add(userKeyword.getName());
        }

        return ProfileInfoDto.builder()
                .user_id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .profilePath(account.getProfilePath())
                .point(account.getPoint())
                .keywords(keywordList)
                .build();
    }

//    public ProfileQuizStatusDto getQuizStatus(Long userId) {
//        Accounts account = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));
//
//        List<Quiz> quizList = accountsRepository.findQuizListByUserId(userId);
//        List<QuizDTO> quizDTOList = new ArrayList<>();
//        for (Quiz quiz : quizList) {
//            quizDTOList.add(new QuizDTO(quiz));
//        }
//
//        List<Integer> quizStatus = accountsRepository.findQuizStatusByUserId(userId);
//
//        return ProfileQuizStatusDTO.builder()
//                .user_id(account.getId())
//                .quizList(quizDTOList)
//                .quizStatus(quizStatus)
//                .build();
//    }

    public ProfileAttendanceListDto getAttendanceList(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        List<Boolean> attendanceList = accountsRepository.findAttendanceListByUserId(userId);

        return ProfileAttendanceListDto.builder()
                .user_id(account.getId())
                .attendanceList(attendanceList)
                .build();
    }


    public Pageable getPageInfo(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("uploadDate"));
        return PageRequest.of(page - 1, 9, Sort.by(sorts));
    }

    public Page<BaseNewsThumbnailDto> getScrapNewsThumbnail(Long userId, int page) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Pageable pageable = getPageInfo(page);

        // 스크랩 된 뉴스 가져와
        Page<Basenews> scrapNewsPage = accountsRepository.findScrapNewsByUserId(userId, pageable);

        return scrapNewsPage.map(basenews -> BaseNewsThumbnailDto.builder()
                .basenewsId(basenews.getId())
                .category(basenews.getCategory().getName())
                .subCategory(basenews.getSubCategory().getName())
                .keyword(basenews.getKeyword().getName())
                .title(basenews.getTitle())
                .summary(basenews.getSummary())
                .imageUrl(basenews.getImageUrl())
                .date(basenews.getUploadDate().toString())
                .isScrap(true)
                .build()
        );
    }

    public Map<String, List<ProfileInterestDto>> getInterest(Long userId) {
        Accounts account = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Map<String, List<ProfileInterestDto>> result = new HashMap<>();

        Pageable three = PageRequest.of(0,3);

        result.put("total", new ArrayList<>());
        result.put("politics", new ArrayList<>());
        result.put("economy", new ArrayList<>());
        result.put("society", new ArrayList<>());

        // 카테고리 상관 없이 전체에서 3개 가져오기
        List<Object[]> totalInterest = accountsRepository.findTotalInterestByUserId(userId, three);
        List<ProfileInterestDto> totalInterestDTO = getPercentage(totalInterest);
        result.put("total", totalInterestDTO); // key를 total로

        // 카테고리 별로 3개 가져오기
        List<Object[]> societyInterest = accountsRepository.findCategoryInterestByUserId(userId, "society", three);
        List<Object[]> politicsInterest = accountsRepository.findCategoryInterestByUserId(userId, "politics", three);
        List<Object[]> economyInterest = accountsRepository.findCategoryInterestByUserId(userId, "economy", three);


        List<ProfileInterestDto> societyInterestDTO = getPercentage(societyInterest);
        List<ProfileInterestDto> politicsInterestDTO = getPercentage(politicsInterest);
        List<ProfileInterestDto> economyInterestDTO = getPercentage(economyInterest);

        result.put("society", societyInterestDTO);
        result.put("politics", politicsInterestDTO);
        result.put("economy", economyInterestDTO);

        return result;
    }

    private List<ProfileInterestDto> getPercentage(List<Object[]> interests) {
        List<ProfileInterestDto> interestDTOList = new ArrayList<>();

        int total = 0;
        for (Object[] item : interests) {
            total += (int) item[2];
        }

        int percentageSum = 0;

        for (Object[] item : interests) {
            String category = (String) item[0];
            String subCategory = (String) item[1];
            int count = (int) item[2];

            int percentage = (int) Math.round((count * 100.0) / total);
            ProfileInterestDto dto = ProfileInterestDto.builder()
                    .category(category)
                    .subCategory(subCategory)
                    .percentage(percentage)
                    .build();
            interestDTOList.add(dto);

            percentageSum += percentage;

        }
        int difference = 100 - percentageSum;

        // 퍼센트 합 100 안되면 제일 큰 항목에 그 차이만큼 더해주기
        if (difference != 0 && !interestDTOList.isEmpty()) {
            ProfileInterestDto largest = interestDTOList.get(0);
            largest.setPercentage(largest.getPercentage() + difference);
        }
        return interestDTOList;
    }

//    private String editProfile(Long userId, ProfileEditDTO profileEditDTO) {
//
//    }
}