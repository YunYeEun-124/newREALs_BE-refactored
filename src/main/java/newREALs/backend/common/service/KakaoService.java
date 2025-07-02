package newREALs.backend.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.accounts.domain.Report;
import newREALs.backend.accounts.domain.UserKeyword;
import newREALs.backend.accounts.dto.ReportCompareDto;
import newREALs.backend.accounts.dto.ReportInterestDto;
import newREALs.backend.accounts.repository.*;
import newREALs.backend.news.repository.KeywordRepository;
import newREALs.backend.news.service.ChatGPTService;
import newREALs.backend.news.service.InsightService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class

KakaoService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    // applications.yml에서 값 가져오기
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;



    public Map<String, Object> processKakaoLogin(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        Map<String, Object> userInfo = getUserInfo(accessToken);


        String email = Optional.ofNullable((String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email"))
                .orElseThrow(() -> new IllegalArgumentException("이메일 정보가 없습니다."));

        String name = (String) ((Map<String, Object>) userInfo.get("properties")).get("nickname");
        String profilePath = (String) ((Map<String, Object>) userInfo.get("properties")).get("profile_image");

        Optional<Accounts> existingAccount = userRepository.findByEmail(email);
        Accounts account = null;
        Map<String, Object> response = new HashMap<>();


        if (existingAccount.isPresent()) {
            //유저 존재 (관심사 등록까지 마침) - accessToken,refreshToken 전달
            account = existingAccount.get();
        } else {
            //임시토큰만 발급해서 리턴. 유저정보 저장X
            response.put("name", name);
            response.put("profile_path", profilePath);
            response.put("email", email);
            response.put("isNewAccount",true);
            response.put("tempToken",tokenService.generateTemporaryToken(email,name,profilePath));
            return response;
        }

        String jwtToken = tokenService.generateAccessToken(account);
        String refreshToken=tokenService.generateRefreshToken(account);
        response.put("isNewAccount",false);
        response.put("name", account.getName());
        response.put("email", account.getEmail());
        response.put("userId", account.getId());
        response.put("accessToken", jwtToken);
        response.put("refreshToken", refreshToken);

        return response;
    }

    // Access Token 받아오기
    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + authorizationCode;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        assert responseBody != null;
        return (String) responseBody.get("access_token");
    }

    // 사용자 정보 가져오기
    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    @Service
    @RequiredArgsConstructor
    public static class ReportService {
        private final UserRepository userRepository;
        private final UserKeywordRepository userKeywordRepository;
        private final KeywordRepository keywordRepository;
        private final ChatGPTService chatGPTService;
        private final SubInterestRepository subInterestRepository;
        private final PreSubInterestRepository preSubInterestRepository;
        private final InsightService insightService;

        public Map<String, Object> generateReportData(Long userId) {
            // 서현
            Map<String, List<ReportInterestDto>> interest = getReportInterest(userId);
            Map<String, Object> change = getChangeGPT(userId);
            Map<String, List<ReportCompareDto>> compare = getReportCompareLast(userId);
            // 현진 - GPT 추가해야됨
            List<String> keyword = recommendNewKeyword(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("user_id", userId);
            data.put("interest", interest);
            data.put("change", change);
            data.put("compare", compare);
            data.put("keyword", keyword);

            String finalSummary=generateFinalSummary(userId,new HashMap<>(data));
            data.put("finalSummary",finalSummary);
            return data;


        }

        public String generateFinalSummary(Long userId, Map<String, Object> reportData) {
            // OpenAI ChatGPT 요청 메시지 생성
            List<Map<String, String>> message = new ArrayList<>();
            message.add(Map.of("role", "system", "content",
                    "아래의 데이터를 기반으로 유저의 이번 달 관심도 분석을 요약해줘. "
                            + "친절하고 이해하기 쉽게 3-4문장으로 요약하고, '~했어요'라는 말투로 작성해줘. "
                            + "분석 결과는 관심도, 관심 변화, 활동 비교 등의 내용을 포함해야 해. "
                            + "주요 내용은 아래 데이터야:"));

            // 전달할 데이터 구성
            String dataSummary = reportData.toString();
            message.add(Map.of("role", "user", "content", dataSummary));

            // ChatGPT API 호출
            String result;
    //        try {
    //            result = (String) chatGPTService.generateContent(message).get("text");
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            result = "분석 내용을 생성하는데 문제가 발생했어요.";
    //        }
            result = (String) chatGPTService.generateContent(message).get("text");

            return result;
        }


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



        // 레포트 - 관심도 분석 부분
        public Map<String, List<ReportInterestDto>> getReportInterest(Long userId) {
            Map<String, List<ReportInterestDto>> result = new HashMap<>();

            result.put("사회", new ArrayList<>());
            result.put("정치", new ArrayList<>());
            result.put("경제", new ArrayList<>());

            int societyCount = subInterestRepository.findCountByUserIdAndCategory(userId, "사회");
            int politicsCount = subInterestRepository.findCountByUserIdAndCategory(userId, "정치");
            int economyCount = subInterestRepository.findCountByUserIdAndCategory(userId, "경제");

            List<Integer> categoryCount = new ArrayList<>();
            categoryCount.add(societyCount);
            categoryCount.add(politicsCount);
            categoryCount.add(economyCount);

            int totalCount = societyCount + politicsCount + economyCount;

            int societyQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "사회");
            int societyComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "사회");
            int societyScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "사회");

            int politicsQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "정치");
            int politicsComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "정치");
            int politicsScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "정치");

            int economyQuiz = subInterestRepository.findQuizCountByUserIdAndCategory(userId, "경제");
            int economyComment = subInterestRepository.findCommentCountByUserIdAndCategory(userId, "경제");
            int economyScrap = subInterestRepository.findScrapCountByUserIdAndCategory(userId, "경제");

            List<Integer> percentage = getReportPercentage(categoryCount, totalCount);

            result.get("사회").add(ReportInterestDto.builder()
                    .percentage(percentage.get(0))
                    .quiz(societyQuiz)
                    .insight(societyComment)
                    .scrap(societyScrap)
                    .build());

            result.get("정치").add(ReportInterestDto.builder()
                    .percentage(percentage.get(1))
                    .quiz(politicsQuiz)
                    .insight(politicsComment)
                    .scrap(politicsScrap)
                    .build());

            result.get("경제").add(ReportInterestDto.builder()
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

        // 관심 변화 트렌드
        // 지난 달 데이터 0이면 null로
        public Map<String, Object> getReportChange(Long userId) {
            Map<String, Object> result = new HashMap<>();
            result.put("GPTComment", "");

            Integer lastSociety = preSubInterestRepository.findCountByUserIdAndCategory(userId, "사회");
            Integer lastPolitics = preSubInterestRepository.findCountByUserIdAndCategory(userId, "정치");
            Integer lastEconomy = preSubInterestRepository.findCountByUserIdAndCategory(userId, "경제");

            boolean hasLastSocietyData = lastSociety != null && lastSociety > 0;
            boolean hasLastPoliticsData = lastPolitics != null && lastPolitics > 0;
            boolean hasLastEconomyData = lastEconomy != null && lastEconomy > 0;

            boolean hasNoLastData = !hasLastSocietyData && !hasLastPoliticsData && !hasLastEconomyData;
            result.put("hasNoLastData", hasNoLastData);

            Integer thisSociety = subInterestRepository.findCountByUserIdAndCategory(userId, "사회");
            Integer thisPolitics = subInterestRepository.findCountByUserIdAndCategory(userId, "정치");
            Integer thisEconomy = subInterestRepository.findCountByUserIdAndCategory(userId, "경제");

            Map<String, Integer> changeMap = new HashMap<>();
            if (hasLastSocietyData) {
                changeMap.put("사회", getChangeInt(lastSociety, thisSociety));
            }
            else {
                changeMap.put("사회", null);
            }

            if (hasLastPoliticsData) {
                changeMap.put("정치", getChangeInt(lastPolitics, thisPolitics));
            }
            else {
                changeMap.put("정치", null);
            }

            if (hasLastEconomyData) {
                changeMap.put("경제", getChangeInt(lastEconomy, thisEconomy));
            }
            else {
                changeMap.put("경제", null);
            }

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

        public Map<String, Object> getChangeGPT(Long userId) {
            long startTime = System.nanoTime();
            System.out.println("분석 레포트 Change GPT 호출 시작");

            Map<String, Object> changeData = getReportChange(userId);
            String stringChangeData = changeData.toString();
            String userName = userRepository.findById(userId).get().getName();

            List<Map<String, String>> message = new ArrayList<>();
            message.add(Map.of("role", "system", "content",
                    "유저의 지난 달과 이번 달의 활동을 비교한 결과를 보고 유저가 이해하기 쉽게 3문장 정도로 분석해줘."));
            message.add(Map.of("role", "system", "content",
                    "다음은 유저의 지난 달, 이번 달의 활동을 비교한 데이터야: \n"
                            + stringChangeData
                            + userName
                            + "데이터는 다음과 같은 필드로 구성되어 있어: \n"

                            + "**정치**, **사회**, **경제** : 해당 카테고리에서의 뉴스 활동 변화율.\n 값이 양수면 그만큼 활동 증가, 음수면 그만큼 활동 감소, null이면 해당 카테고리는 지난 달 활동이 없는 거야.\n"
                            + "**userName** : 유저의 이름"

                            + "**biggest** : 유저의 활동량 변화량이 큰 카테고리야.\n"
                            + "**hasNoLastData** : 지난 달에 세 카테고리 모두 지난 달 활동이 없었는지를 보여주는 boolean형 데이터야.\n"
                            + "너가 해야하는 건 다음과 같아.\n"
                            + "1. 만약 모든 카테고리가 null이라면 지난 달에 활동이 없었던거라 의미있는 분석이 어렵다는 걸 언급해줘.\n"
                            + "2. 분석에는 가장 변화가 컸던 카테고리, 지난 달 데이터가 없었다면 그거에 대한 언급도 되어있으면 좋겠어.\n"
                            + "3. 결과를 바탕으로 유저에게 활동 변화에 대한 간략한 분석을 해줘.\n"
                            + "4. '~했어요'와 같은 말투로 분석 결과를 3줄로 알려줘.\n"
                            + "5. 단순히 숫자 전달만 하는 게 아니라 그 숫자에 대한 분석이 필요해.\n"
                            + "6. 사용자의 각 카테고리에 대한 활동 변화가 어땠는지 간략하게 서술하고, 이를 통해 어떤 걸 알 수 있는지 말해줬으면 좋겠어.\n"
                            + "7. 분석 예시: 이번 11월, 서현님은 사회 분야에 가장 큰 관심을 보였어요. 특히, 전세사기와 같은 주거 안정 문제와 강력 범죄 관련 이슈에 대해 높은 참여을 보였어요. 경제 분야에서도 일부 관심이 있었지만, 생활과 직접적으로 연관된 사회적 문제에 대한 관심이 가장 두드러졌어요."));

            String result = (String) chatGPTService.generateContent(message).get("text");
            changeData.put("GPTComment", result);

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            System.out.println("GPT 실행 시간: " + duration + "ms");

            return changeData;
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class ReportSaveService {
        private final UserRepository userRepository;
        private final ReportRepository reportRepository;
        private final ReportService reportService;
        private final ObjectMapper objectMapper;

        // 레포트 생성
        @Transactional
        @Scheduled(cron = "00 17 01 01 * ?")
        public void makeReports() throws JsonProcessingException {
            List<Accounts> users = userRepository.findAll();
            for(Accounts user : users) {
                Map<String, Object> reportData = reportService.generateReportData(user.getId());
                String jsonData = objectMapper.writeValueAsString(reportData);

                Report report = reportRepository.findByUserId(user.getId())
                        .orElse(null);
                if(report != null) {
                    reportRepository.updateReport(user.getId(), jsonData);
                }
                else {
    //                report = new Report();
    //                report.setUser(user);
    //                report.setReport(jsonData);
                    reportRepository.saveReport(user.getId(), jsonData);
                }
    //            reportRepository.save(report);
            }
        }

    }
}