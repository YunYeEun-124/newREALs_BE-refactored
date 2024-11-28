package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.dto.*;
import newREALs.backend.domain.*;
import newREALs.backend.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final AccountsRepository accountsRepository;
    private final ScrapRepository scrapRepository;
    private final BaseNewsRepository baseNewsRepository;
    private final SubInterestRepository subInterestRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final S3Service s3Service;
    private final PreSubInterestRepository preSubInterestRepository;


    //[get] 프로필 정보
    public ProfileInfoDto getProfileInfo(Long userId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 유저 키워드 리스트에 저장하기
        List<Keyword> userKeywords = userKeywordRepository.findKeywordsById(userId);
        List<String> keywordList = new ArrayList<>();
        for(Keyword userKeyword : userKeywords){
            keywordList.add(userKeyword.getName());
        }

        return ProfileInfoDto.builder()
                .user_id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePath(user.getProfilePath())
                .point(user.getPoint())
                .keywords(keywordList)
                .build();
    }


    //[get] 출석현황
    public ProfileAttendanceListDto getAttendanceList(Long userId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        List<Boolean> attendanceList = accountsRepository.findAttendanceListById(userId);

        return ProfileAttendanceListDto.builder()
                .user_id(user.getId())
                .attendanceList(attendanceList)
                .build();
    }


    public Pageable getPageInfo(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("basenews.uploadDate"));
        return PageRequest.of(page - 1, 6, Sort.by(sorts));
    }

    //[get] 스크랩 보여주기
    public Page<BaseNewsThumbnailDTO> getScrapNewsThumbnail(Long userId, int page) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Pageable pageable = getPageInfo(page);

        // 스크랩 된 뉴스 가져와
        //Page<Basenews> scrapNewsPage = accountsRepository.findScrapNewsById(userId, pageable);
        Page<Scrap> scrapNewsList = scrapRepository.findByUser(user, pageable);

        return scrapNewsList.map(scrap -> {
            Basenews basenews = scrap.getBasenews();
            return BaseNewsThumbnailDTO.builder()
                    .basenewsId(basenews.getId())
                    .category(basenews.getCategory().getName())
                    .subCategory(basenews.getSubCategory().getName())
                    .keyword(basenews.getKeyword().getName())
                    .title(basenews.getTitle())
                    .summary(basenews.getSummary())
                    .imageUrl(basenews.getImageUrl())
                    .date(basenews.getUploadDate()) // Basenews의 uploadDate 필드
                    .isScrapped(true)
                    .build();
        });
    }


    //관심도 분석
    public Map<String, List<ProfileInterestDto>> getInterest(Long userId) {
        Map<String, List<ProfileInterestDto>> result = new HashMap<>();

        result.put("total", new ArrayList<>());
        result.put("politics", new ArrayList<>());
        result.put("economy", new ArrayList<>());
        result.put("society", new ArrayList<>());

        // 카테고리 상관 없이 전체에서 3개 가져오기
        List<ProfileInterestProjection> totalInterest = accountsRepository.findTotalInterestById(userId);
        List<ProfileInterestDto> totalInterestDTO = getPercentage(totalInterest);
        result.put("total", totalInterestDTO); // key를 total로

        // 카테고리 별로 3개 가져오기
        List<ProfileInterestProjection> societyInterest = accountsRepository.findCategoryInterestById(userId, "society");
        List<ProfileInterestProjection> politicsInterest = accountsRepository.findCategoryInterestById(userId, "politics");
        List<ProfileInterestProjection> economyInterest = accountsRepository.findCategoryInterestById(userId, "economy");

        List<ProfileInterestDto> societyInterestDTO = getPercentage(societyInterest);
        List<ProfileInterestDto> politicsInterestDTO = getPercentage(politicsInterest);
        List<ProfileInterestDto> economyInterestDTO = getPercentage(economyInterest);

        result.put("society", societyInterestDTO);
        result.put("politics", politicsInterestDTO);
        result.put("economy", economyInterestDTO);

        return result;
    }



    //[get] 관심도 분석 비율 찾기
    private List<ProfileInterestDto> getPercentage(List<ProfileInterestProjection> interests) {
        List<ProfileInterestDto> interestDTOList = new ArrayList<>();

        int total = 0;
        for (ProfileInterestProjection item : interests) {
            total += item.getCount();
        }
        if(total == 0) {
            return interestDTOList;
        }

        int percentageSum = 0;

        for (ProfileInterestProjection item : interests) {
            String category = item.getCategory();
            String subCategory = item.getSubCategory();
            int count = item.getCount();

            int percentage = (int) Math.round((count * 100.0) / total);
            if (percentage > 0) {
                ProfileInterestDto dto = ProfileInterestDto.builder()
                        .category(category)
                        .subCategory(subCategory)
                        .percentage(percentage)
                        .build();
                interestDTOList.add(dto);

                percentageSum += percentage;
            }

        }
        int difference = 100 - percentageSum;

        // 퍼센트 합 100 안되면 제일 큰 항목에 그 차이만큼 더해주기
        if (difference != 0 && !interestDTOList.isEmpty()) {
            ProfileInterestDto largest = interestDTOList.get(0);
            largest.setPercentage(largest.getPercentage() + difference);
        }
        return interestDTOList;
    }
    public Map<String, List<ReportInterestDto>> getReportInterest(Long userId) {
        Map<String, List<ReportInterestDto>> result = new HashMap<>();

        result.put("society", new ArrayList<>());
        result.put("politics", new ArrayList<>());
        result.put("economy", new ArrayList<>());

        int societyCount = subInterestRepository.findCountByUserIdAndCategory(userId, "society");
        int politicsCount = subInterestRepository.findCountByUserIdAndCategory(userId, "politics");
        int economyCount = subInterestRepository.findCountByUserIdAndCategory(userId, "economy");

        List<Integer> categoryCount = new ArrayList<>();
        categoryCount.add(societyCount);
        categoryCount.add(politicsCount);
        categoryCount.add(economyCount);

        int totalCount = societyCount + politicsCount + economyCount;

        int societyQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "society");
        int societyComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "society");
        int societyScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "society");

        int politicsQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "politics");
        int politicsComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "politics");
        int politicsScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "politics");

        int economyQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "economy");
        int economyComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "economy");
        int economyScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "economy");

        List<Integer> percentage = getReportPercentage(categoryCount, totalCount);

        result.get("society").add(ReportInterestDto.builder()
                .percentage(percentage.get(0))
                .quiz(societyQuiz)
                .insight(societyComment)
                .scrap(societyScrap)
                .build());

        result.get("politics").add(ReportInterestDto.builder()
                .percentage(percentage.get(1))
                .quiz(politicsQuiz)
                .insight(politicsComment)
                .scrap(politicsScrap)
                .build());

        result.get("economy").add(ReportInterestDto.builder()
                .percentage(percentage.get(2))
                .quiz(economyQuiz)
                .insight(economyComment)
                .scrap(economyScrap)
                .build());

        return result;
    }

    private List<Integer> getReportPercentage(List<Integer> catCount, int totCount) {
        List<Integer> percentages = new ArrayList<>();
        if (totCount == 0) {
            return List.of(0, 0, 0);
        }

        int percentageSum = 0;
        int maxIndex = -1;
        int maxValue = 0;

        for (int i = 0; i < catCount.size(); i++) {
            int count = catCount.get(i);
            int percentage = (int) Math.round((count * 100.0) / totCount);
            percentages.add(percentage);
            percentageSum += percentage;

            if (count > maxValue) {
                maxValue = count;
                maxIndex = i;
            }
        }

        // 퍼센트 합 100 안되면 제일 큰 항목에 그 차이만큼 더해주기
        int difference = 100 - percentageSum;
        if (difference != 0 && maxIndex != -1) {
            percentages.set(maxIndex, percentages.get(maxIndex) + difference);
        }

        return percentages;
    }

    public void editProfile(Long userId, String newName, MultipartFile file) throws IOException {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        // 이름 변경

        if (newName != null && !newName.isEmpty()) {
            user.setName(newName);
        }

        if(file!=null&&!file.isEmpty()) {
            String s3Url = s3Service.uploadFile(file);
            user.setProfilePath(s3Url);
        }

//        if (file != null && !file.isEmpty()) {
//            File directory = new File(uploadDir);
//
//            if (!directory.exists()) {
//                if (!directory.mkdirs()) {
//                    throw new IOException("디렉토리를 생성할 수 없습니다: " + uploadDir);
//                }
//            }

//            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//            File saveFile = new File(uploadDir + fileName);
//            file.transferTo(saveFile);
//
//            // 일단 로컬로 설정
//            String newProfilePath = "http://localhost:8080/" + uploadDir + fileName;
//            user.setProfilePath(newProfilePath);

        userRepository.save(user);
    }

    public boolean isScrapped(Long userId, Long newsId) {
        return scrapRepository.existsByUser_IdAndBasenews_Id(userId, newsId);
    }

    public void deleteScrap(Long userId, Long newsId) {
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));
        Basenews news = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("없는 뉴스 ID"));

        Scrap scrap = scrapRepository.findByUserAndBasenews(user, news)
                .orElseThrow(() -> new IllegalArgumentException("스크랩된 뉴스가 아닙니다."));

        int keywordId = news.getKeyword().getId().intValue();

        user.updateKeywordInterest(keywordId, -2);
        userRepository.save(user);

        SubInterest subInterest = subInterestRepository.findByUserAndSubCategoryId(user, news.getSubCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("subInterest 없음"));

            subInterest.setScrapCount(subInterest.getScrapCount() - 1);
            subInterestRepository.save(subInterest);
        // scrap 삭제
        scrapRepository.delete(scrap);

    }

    public Page<BaseNewsThumbnailDTO> getScrapSearchList(Long userId, String keyword, int page){
        Accounts user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 userId"));

        Pageable pageable = getPageInfo(page);

        Page<Basenews> scrapSearchList = scrapRepository.findByUserAndTitleContainingOrDescriptionContaining(userId, keyword, pageable);

        return scrapSearchList.map(basenews -> BaseNewsThumbnailDTO.builder()
                .basenewsId(basenews.getId())
                .category(basenews.getCategory().getName())
                .subCategory(basenews.getSubCategory().getName())
                .keyword(basenews.getKeyword().getName())
                .title(basenews.getTitle())
                .summary(basenews.getSummary())
                .imageUrl(basenews.getImageUrl())
                .date(basenews.getUploadDate())
                .isScrapped(true)
                .build());
    }
    // 지난 달 데이터 0이면 null로
    public Map<String, Object> getReportChange(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("GPTComment", "GPT 응답 넣을게요");

        Integer lastSociety = preSubInterestRepository.findCountByUserIdAndCategory(userId, "society");
        Integer lastPolitics = preSubInterestRepository.findCountByUserIdAndCategory(userId, "politics");
        Integer lastEconomy = preSubInterestRepository.findCountByUserIdAndCategory(userId, "economy");

        boolean hasLastSocietyData = lastSociety != null && lastSociety > 0;
        boolean hasLastPoliticsData = lastPolitics != null && lastPolitics > 0;
        boolean hasLastEconomyData = lastEconomy != null && lastEconomy > 0;

        boolean hasNoLastData = !hasLastSocietyData && !hasLastPoliticsData && !hasLastEconomyData;
        result.put("hasNoLastData", hasNoLastData);

        Integer thisSociety = subInterestRepository.findCountByUserIdAndCategory(userId, "society");
        Integer thisPolitics = subInterestRepository.findCountByUserIdAndCategory(userId, "politics");
        Integer thisEconomy = subInterestRepository.findCountByUserIdAndCategory(userId, "economy");

        Map<String, Integer> changeMap = new HashMap<>();
        changeMap.put("society", hasLastSocietyData ? getChangeInt(lastSociety, thisSociety) : null);
        changeMap.put("politics", hasLastPoliticsData ? getChangeInt(lastPolitics, thisPolitics) : null);
        changeMap.put("economy", hasLastEconomyData ? getChangeInt(lastEconomy, thisEconomy) : null);

        String biggest = null;
        Integer maxChange = null;

        for (Map.Entry<String, Integer> entry : changeMap.entrySet()) {
            Integer value = entry.getValue();
            if (value != null && (maxChange == null || value > maxChange)) {
                maxChange = value;
                biggest = entry.getKey();
            }
        }

        if (biggest != null) {
            result.put("biggest", biggest);
        }

        result.putAll(changeMap);
        return result;
    }

    // 증가율 계산
    private int getChangeInt(int lastMonth, int thisMonth) {
        if (thisMonth == 0) {
            if (lastMonth == 0) {
                return 0;
            } else {
                return -100;
            }
        }

        if (lastMonth == 0) {
            return 100;
        }

        int difference = thisMonth - lastMonth;
        double percentageChange = ((double) difference / lastMonth) * 100;

        return (int) Math.round(percentageChange);
    }

    // 지난 달이랑 비교
    public Map<String, List<ReportCompareDto>> getReportCompareLast(Long userId) {
        Map<String, List<ReportCompareDto>> result = new HashMap<>();
        result.put("lastMonth", new ArrayList<>());
        result.put("thisMonth", new ArrayList<>());

        int lastQuiz = preSubInterestRepository.findTotalQuizCountByUserId(userId);
        int lastComment = preSubInterestRepository.findTotalCommentCountByUserId(userId);
        int lastAtt = preSubInterestRepository.findTotalAttCountByUserId(userId);

        int thisQuiz = subInterestRepository.findTotalQuizCountByUserId(userId);
        int thisComment = subInterestRepository.findTotalCommentCountByUserId(userId);
        int thisAtt = subInterestRepository.findTotalAttCountByUserId(userId);

        result.get("lastMonth").add(ReportCompareDto.builder()
                        .quiz(lastQuiz)
                        .insight(lastComment)
                        .attendance(lastAtt)
                .build());
        result.get("thisMonth").add(ReportCompareDto.builder()
                .quiz(thisQuiz)
                .insight(thisComment)
                .attendance(thisAtt)
                .build());
        return result;

    }
}