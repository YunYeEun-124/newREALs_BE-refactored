package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.dto.ReportCompareDto;
import newREALs.backend.dto.ReportInterestDto;
import newREALs.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final KeywordRepository keywordRepository;
    private final ChatGPTService chatGPTService;
    private final SubInterestRepository subInterestRepository;
    private final PreSubInterestRepository preSubInterestRepository;


    public String getAnalysisSummary(Long userId){
        String summary=null;
        return summary;
    }
    public List<String> recommendNewKeyword(Long userId){
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        //관심키워드 가져옴. 인덱스=id Long 변환
        Map<Long, Integer> keywordMap = new HashMap<>();
        for (int i = 0; i < user.getKeywordInterest().size(); i++) {
            keywordMap.put((long)(i + 1), user.getKeywordInterest().get(i)); // ID는 1부터 시작
        }
        //이미 등록된 키워드 가져옴
        List<UserKeyword> userKeywords=userKeywordRepository.findAllByUserId(userId);
        Set<Long> excludeKeywordIds=userKeywords.stream()
                .map(UserKeyword::getKeywordId)
                .collect(Collectors.toSet());
        //이미 등록된 키워드 제외하고 상위 5개 찾기
        List<Long> keywordIDs= keywordMap.entrySet().stream()
                .filter(entry->!excludeKeywordIds.contains(entry.getKey()))
                .sorted((e1,e2)->e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        //ID->이름 변환
        return keywordIDs.stream()
                .map(keywordRepository::findNameById)
                .collect(Collectors.toList());

    }


    @Transactional
    public void processReport(Long userId) throws Throwable{
        //시작시간
        long startTime = System.nanoTime();
        System.out.println("processReport in ");
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        //1. 서현언니 분석 + 2.
        List<Map<String,String>> Messages=new ArrayList<>();
        Messages.add(Map.of("role","system","content",
                "..."));

        Messages.add(Map.of("role", "user", "content",
                "해야할 일이 크게 2가지이다. "
        ));

        String result=(String) chatGPTService.generateContent(Messages).get("text");
        System.out.println("gpt result");
        System.out.println(result);

        //처이 완료 시간
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 밀리초로 변환

        System.out.println("Execution time for processArticle: " + duration + " ms");
    }

    // 레포트 - 관심도 분석 부분
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

    // 지난 달이랑 활동 비교
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
