package newREALs.backend.service;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Quiz;
import newREALs.backend.domain.TermDetail;
import newREALs.backend.repository.BasenewsRepository;
import newREALs.backend.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
public class NewsService {
    private final ChatGPTService chatGPTService;
    private final BasenewsRepository basenewsRepository;
    private final QuizRepository quizRepository;

    public NewsService(ChatGPTService chatGPTService, BasenewsRepository basenewsRepository, QuizRepository quizRepository) {
        this.chatGPTService = chatGPTService;
        this.basenewsRepository = basenewsRepository;
        this.quizRepository = quizRepository;
    }

    //용어 파싱 메서드
    private List<TermDetail> parseTerms(String termsContent) {
        List<TermDetail> termDetails = new ArrayList<>();

        //용어 설명 세트분리하기 (줄바꿈 기준)
        String[] termsArray = termsContent.split("\\n");
        for (String termPair : termsArray) {
            termPair = termPair.replaceAll("\\d+\\.\\s*", ""); // 번호 제거
            String[] termAndDescription = termPair.split(":", 2);  // 첫 번째 콜론 기준으로 용어와 설명 구분
            if (termAndDescription.length == 2) {
                String term = termAndDescription[0].trim();
                String termDescription = termAndDescription[1].trim();
                termDetails.add(new TermDetail(term, termDescription));
            }
        }
        return termDetails;
    }

    //요약, 설명, 용어 생성 메서드
    @Transactional
    public void processArticle(Long basenewsId) throws Throwable {
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid news ID"));

        // 요약 생성 summary
        List<Map<String, String>> summaryMessages = new ArrayList<>();
        summaryMessages.add(Map.of("role", "system", "content", "You are a helpful assistant."));
        summaryMessages.add(Map.of("role", "user", "content", "다음 뉴스 기사를 3문장 내로 요약해 주세요.: " + basenews.getDescription()));

        // 설명 생성 description
        List<Map<String, String>> explanationMessages = new ArrayList<>();
        explanationMessages.add(Map.of("role", "system", "content", "You are a helpful assistant."));
        explanationMessages.add(Map.of("role", "user", "content", "다음 뉴스 기사를 이해하기 쉽게 간단히 설명해 주세요: " + basenews.getDescription()));

        // 용어 리스트 생성 termList
        List<Map<String, String>> termsMessages = new ArrayList<>();
        termsMessages.add(Map.of("role", "system", "content", "너는 '~해요'체를 쓰는 유능한 어시스턴트야"));
        termsMessages.add(Map.of("role", "user", "content", "다음 뉴스 기사에서 어려운 용어 5개를 뽑아 각 용어에 대해 간단히 설명해 주세요. 각 용어마다 설명은 1~2문장이어야 합니다. '~해요'체를 사용해서 설명해주세요.: " + basenews.getDescription()));

        // GPT 서비스 호출
        String summary = (String) chatGPTService.generateContent(summaryMessages).get("text");
        String explanation = (String) chatGPTService.generateContent(explanationMessages).get("text");
        String termsContent = (String) chatGPTService.generateContent(termsMessages).get("text");

        // 용어 -> TermDetail 변환
        List<TermDetail> termDetails = parseTerms(termsContent);

        // 데이터 저장
        basenews.setSummary(summary.length() > 255 ? summary.substring(0, 255) : summary);
        basenews.setDescription(explanation);  // 설명 필드에는 전체 설명 저장
        basenews.setTermList(termDetails);  // termList에 용어 리스트 저장
        basenewsRepository.save(basenews);
    }


    //퀴즈 생성하는 메서드
    @Transactional
    public void generateAndSaveQuizzesForDailyNews() {
        // 1. isDailynews=true인 basenews 가져오기
        List<Basenews> dailyNewsList = basenewsRepository.findByIsDailyNewsTrue();

        for (Basenews news : dailyNewsList) {
            // 2. GPT를 통해 문제, 정답, 해설 생성 요청
            List<Map<String, String>> quizMessages = new ArrayList<>();
            quizMessages.add(Map.of("role", "system", "content", "You are a helpful assistant that generates quizzes."));
            quizMessages.add(Map.of("role", "user", "content",
                    "다음 기사의 설명을 읽고 true/false 문제를 만들어 주세요.\n" +
                            "문제, 정답(O 또는 X), 해설을 다음과 같은 형식으로 제공해주세요:\n" +
                            "문제: <문제 내용>\n" +
                            "정답: <O 또는 X>\n" +
                            "해설: <해설 내용>\n\n" +
                            "기사 설명: " + news.getDescription()));

            String quizContent = (String) chatGPTService.generateContent(quizMessages).get("text");

            // 3. GPT 응답 파싱
            Map<String, String> parsedQuiz = parseQuizContent(quizContent);

            // 4. Quiz 엔티티 생성 및 저장
            Quiz quiz = Quiz.builder()
                    .p(parsedQuiz.get("problem"))
                    .a("O".equalsIgnoreCase(parsedQuiz.get("answer")))
                    .comment(parsedQuiz.get("comment"))
                    .basenews(news)
                    .build();

            quizRepository.save(quiz);
        }
    }

    //퀴즈 파싱 메서드
    private Map<String, String> parseQuizContent(String quizContent) {
        Map<String, String> parsedQuiz = new HashMap<>();
        String[] lines = quizContent.split("\n");

        for (String line : lines) {
            if (line.startsWith("문제:")) {
                parsedQuiz.put("problem", line.replace("문제:", "").trim());
            } else if (line.startsWith("정답:")) {
                parsedQuiz.put("answer", line.replace("정답:", "").trim());
            } else if (line.startsWith("해설:")) {
                parsedQuiz.put("comment", line.replace("해설:", "").trim());
            }
        }

        return parsedQuiz;
    }
}


