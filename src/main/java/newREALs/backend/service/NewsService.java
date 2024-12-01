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




  //  @Scheduled(cron = "0 37 20 ? * *")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void automaticBaseProcess(){
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

        basenewsRepository.saveAll(resultList);

        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        System.out.println("비동기 작업 전체 처리 시간: " + (endTime - startTime) + "ms");
    }



    @Transactional
    public void automaticDailyProcess(){
        // 오늘의 뉴스 5개 찾아와서 퀴즈 생성 + 생각정리 같이 만들기
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
    @Transactional
    public void generateAndSaveQuizzesForDailyNews(Basenews news) {
        // 이미 isDailynews=true인 basenews를 전달받음
        List<Map<String, String>> quizMessages = new ArrayList<>();
        quizMessages.add(Map.of("role", "system", "content",
                "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles."));
        quizMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사의 요약입니다. 이 요약을 바탕으로 기사에 대한 핵심 정보를 묻는 true/false 문제를 만들어 주세요. "
                        + "문제는 반드시 기사의 중요한 내용을 기반으로 해야 합니다. "
                        + "답은 O(참) 또는 X(거짓) 중 하나여야 하며, 문제의 정답과 관련된 배경 설명(해설)을 추가로 작성해주세요. " +
                        "해설은 한 줄만 필요합니다. "
                        + "결과는 아래 형식에 맞춰 작성해 주세요:\n\n"
                        + "문제: <문제 내용>\n"
                        + "정답: <O 또는 X>\n"
                        + "해설: <해설 내용>\n\n"
                        + "기사 요약: " + news.getDescription()));

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
    @Async
    public void generateAndSaveThinkCommentForDailyNews(Basenews news){
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
    @Async
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
