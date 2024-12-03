package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Quiz;
import newREALs.backend.domain.TermDetail;
import newREALs.backend.domain.ThinkComment;
import newREALs.backend.repository.BaseNewsRepository;
import newREALs.backend.repository.InsightRepository;
import newREALs.backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final ChatGPTService chatGPTService;
    private final BaseNewsRepository basenewsRepository;
    private final QuizRepository quizRepository;
    private final InsightRepository insightRepository;
    private final ArticleProcessingService articleProcessingService;
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);


    @Transactional
    public void saveProcessedNews(List<Basenews> processedNews) {
        basenewsRepository.saveAll(processedNews);
    }

   // @Scheduled(cron = "0 15 23 ? * *")
    public void automaticBaseProcess(){
        System.out.println("automaticBaseProcess in ");


        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        List<Basenews> newBasenews = basenewsRepository.findBySummaryIsNull();
        if(newBasenews.isEmpty()) {
            System.out.println("summary null news NOPE");
            return ;
        }

        List<CompletableFuture<Basenews>> futures = new ArrayList<>();
        for(Basenews news : newBasenews)
            futures.add((articleProcessingService.processArticleAsync(news.getId())));

        //futures.add 하는 이유 : 모든 비동기 작업이 완류 된 후 다음 save해야되기 때문
        // 기다리는 작업(allOf) 해야하는데 리스트에 담아서 관리하지 않으면 작업 완료 여부 파악힘들다.
        // and for문 돌리면서 어디서 작업이 실패했는지 파악도 해야함.

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<Basenews> resultList = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();

        saveProcessedNews(resultList);

        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        System.out.println("비동기 작업 전체 처리 시간: " + (endTime - startTime) + "ms");
    }



   // @Transactional
    public void automaticDailyProcess(){
        System.out.println("automaticDailyProcess in");
        for (Basenews news : basenewsRepository.findTop5ByIsDailyNewsTrueOrderByIdDesc()) {
            try {
                generateAndSaveQuizzesForDailyNews(news);
                generateAndSaveThinkCommentForDailyNews(news);
            } catch (Exception e) {
                log.error("Failed to generate quiz for article ID: {}", news.getId(), e);
            }
        }

    }
    //요약, 설명, 용어 생성 메서드


    //퀴즈 생성하는 메서드
    //@Transactional
    public void generateAndSaveQuizzesForDailyNews(Basenews news) {
        // 이미 isDailynews=true인 basenews를 전달받음
        // 1. 이미 해당 뉴스에 대한 퀴즈가 존재하는지 확인
        if (quizRepository.existsByBasenews(news)) {
            log.warn("Quiz already exists for Basenews ID: {}", news.getId());
            return;
        }
        System.out.println("generateAndSaveQuizzesForDailyNews in");

        List<Map<String, String>> quizMessages = new ArrayList<>();
        quizMessages.add(Map.of("role", "system", "content",
                "나는 뉴스 입문자들을 위해 뉴스를 쉽게 풀어 설명하고, 요약과 어려운 용어 설명을 제공해주는 사이트를 운영하고 있다.\n" +
                        "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles. "
                        + "Make sure the questions are challenging yet clear, and directly reflect the content of the article summary provided."
        ));
        quizMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사에 대한 설명이다. 이 설명을 바탕으로 기사에 대한 핵심 정보를 묻는 True/False 문제를 만들어라.\n"
                        + "문제 작성 시 아래 사항을 반드시 준수해야 한다:\n"
                        + "1. 문제는 반드시 기사 내용의 중요한 정보를 기반으로 작성하라.\n"
                        + "2. 각 문제의 정답은 반드시 True (O) 또는 False (X) 중 하나로 명확하게 결정되어야 한다.\n"
                        + "3. 각 문제에 대해 간결하면서도 명확한 해설을 추가하라. 해설은 한 줄로 작성하라. '~해요'체를 사용하여 친절하게 설명하라.\n"
                        + "4. 문제의 난이도는 뉴스 입문자가 이해할 수 있는 수준을 유지하되, 핵심 내용을 강조하라.\n\n"
                        + "출력 형식:\n"
                        + "문제: <문제 내용>\n"
                        + "정답: <O 또는 X>\n"
                        + "해설: <해설 내용>\n\n"
                        + "Example 1:\n"
                        + "기사 설명: 최근 한국경영자총협회에서 실시한 조사에 따르면, 30인 이상의 국내 기업 중 약 50%가 내년에 긴축 경영을 실시할 계획이라고 합니다. 긴축 경영이란 비용 절감과 자원 효율화를 목표로 하는 경영 전략을 의미합니다. 이 조사는 239개의 기업 최고경영자(CEO)와 임원을 대상으로 이루어졌으며, 그 결과 대기업(300인 이상)이 중소기업(300인 미만)보다 긴축 경영을 더 많이 계획하고 있는 것으로 나타났습니다. 이러한 긴축 경영의 구체적인 방법으로는 전사적 원가 절감, 인력 운용의 합리화, 신규 투자의 축소 등이 포함되어 있습니다. 또한, 투자와 채용 계획도 축소하는 경향이 두드러졌으며, 이는 대기업에서 더욱 명확하게 나타났습니다. 한편, 도널드 트럼프 미국 대통령의 재집권이 국내 경제에 부정적 영향을 미칠 것으로 예상되며, 이는 주로 보호무역주의 강화 때문입니다.\n\n"
                        + "문제: 긴축 경영의 구체적인 방법에는 신규 투자의 확대가 포함된다.\n"
                        + "정답: X\n"
                        + "해설: 긴축 경영은 비용 절감과 자원 효율화를 목표로 하며, 신규 투자 확대가 아닌 축소를 포함해요.\n\n"
                        + "기사 요약: " + news.getDescription()
        ));

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

    //ThinkComment generate function
   // @Async
    public void generateAndSaveThinkCommentForDailyNews(Basenews news){
        System.out.println("generateAndSaveThinkCommentForDailyNews in");
        if (insightRepository.existsByBasenews(news)) {
            log.warn("insight already exists for Basenews ID: {}", news.getId());
            return;
        }

        List<Map<String, String>> insightMessages = new ArrayList<>();
        insightMessages.add(Map.of("role", "system", "content",
                "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles."));
        insightMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사의 요약입니다."+
                        "해당 기사로 토론할만한 주제를 선정해주세요. 입문자 수준의 토론 주제로 너무 어렵지 않게 해주세요. 어려운 용어를 사용하지 않거나. "
                        +"사용할 시 쉽게 풀어서 제시 해주세요. 주제 선정과 함께 해당 주제에 대한 당신의 의견도 간략히 써줘 200자 이내로 "
                        + "결과는 아래 형식에 맞춰 작성해 주세요:\n\n"
                        + "토픽: ~~ 어떻게 생각하세요?\n"
                        + "의견: ~ \n"
                        + "기사 요약을 참고해 " + news.getDescription()));

        String quizContent = (String) chatGPTService.generateContent(insightMessages).get("text");
        // "용어" 섹션만 추출
        String[] lines = quizContent.split("\\n");
        String topic =""; String aiComment ="";

        for (String line : lines) {
            if (line.startsWith("토픽:"))  // topic 섹션 처리
                topic = line.substring(3).trim(); // "요약:" 이후 내용
            else if (line.startsWith("의견:"))  // comment 섹션 처리
                aiComment = line.substring(3).trim(); // "설명:" 이후 내용
        }

        insightRepository.save(
          ThinkComment.builder()
                  .topic(topic)
                  .AIComment(aiComment)
                  .basenews(news).build()
        );

        StringTokenizer st = new StringTokenizer(quizContent,":");
        st.nextToken();
    }



    //퀴즈 파싱 메서드
    //@Async
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
