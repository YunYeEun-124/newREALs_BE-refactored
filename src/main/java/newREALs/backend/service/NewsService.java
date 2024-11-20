package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Quiz;
import newREALs.backend.domain.TermDetail;
import newREALs.backend.repository.BaseNewsRepository;
import newREALs.backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final ChatGPTService chatGPTService;
    private final BaseNewsRepository basenewsRepository;
    private final QuizRepository quizRepository;
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    //요약, 설명, 용어, 퀴즈 생성 자동화
    //@Scheduled(cron = "0 7 20 ? * *")
    @Scheduled(cron="0 37 09 ? * * ")//매일 오전 6시 10분 실행
    @Transactional
    @Async
    public void automaticProcess(){
        //basenews들 중 summary=null인 뉴스들 가져옴(새롭게 생성된 뉴스)
        List<Basenews> newBasenews = basenewsRepository.findBySummaryIsNull();
       // Optional<Basenews> newBasenews = basenewsRepository.findFirstBySummaryIsNull();
        for (Basenews news : newBasenews) {
            try {
                //processArticle(newBasenews.get().getId());
                processArticle(news.getId());
            } catch (Throwable e) {
               log.error("Failed to process article ID: {}", news.getId(), e);
            }
        }

        // 오늘의 뉴스 5개 찾아와서 퀴즈 생성 + 생각정리 같이 만들기
        List<Basenews> dailyNews = basenewsRepository.findTop5ByIsDailyNewsTrueOrderByIdDesc();
        for (Basenews news : dailyNews) {
            try {
                generateAndSaveQuizzesForDailyNews(news);
              //  generateAndSaveThinkCommentForDailyNews(news);
            } catch (Exception e) {
                log.error("Failed to generate quiz for article ID: {}", news.getId(), e);
            }
        }

    }



    //요약, 설명, 용어 생성 메서드
    @Transactional
    @Async
    public void processArticle(Long basenewsId) throws Throwable {

        System.out.println("processArticle in ");

        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid news ID"));

        // 요약 생성 summary
        List<Map<String, String>> summaryMessages = new ArrayList<>();
        summaryMessages.add(Map.of("role", "system", "content",
                "You are a professional assistant that specializes in summarizing articles. "
                        + "Your goal is to create concise and clear summaries of news articles in 3 sentences."));
        summaryMessages.add(Map.of("role", "user", "content",
                "다음 뉴스 기사를 핵심만 간결하게 3문장으로 요약해 주세요. "
                        + "각 문장은 완결된 문장이어야 하고, 기사의 주요 내용과 배경이 명확히 드러나도록 작성해주세요. "
                        + "기사 내용은 다음과 같습니다: " + basenews.getDescription()));

        // 설명 생성 description
        List<Map<String, String>> explanationMessages = new ArrayList<>();
        explanationMessages.add(Map.of("role", "system", "content",
                "You are a professional assistant that explains news articles in simple terms. "
                        + "Your goal is to make complex news topics easy to understand for a general audience."));
        explanationMessages.add(Map.of("role", "user", "content",
                "아래 뉴스 기사를 독자가 쉽게 이해할 수 있도록 간단하게 설명해 주세요. "
                        + "핵심 배경, 사건의 원인과 결과를 포함하여 전체 내용을 한눈에 파악할 수 있도록 작성해주세요. "
                        + "설명은 너무 간략하지 않게, 명확하면서도 친절하게 작성해 주세요. "
                        + "기사 내용은 다음과 같습니다: " + basenews.getDescription()));

        // 용어 리스트 생성 termList
        List<Map<String, String>> termsMessages = new ArrayList<>();
        termsMessages.add(Map.of("role", "system", "content",
                "You are a friendly and knowledgeable assistant who uses polite and clear language. "
                        + "Your task is to identify difficult terms in news articles and explain them in a simple and approachable way."));
        termsMessages.add(Map.of("role", "user", "content",
                "다음 뉴스 기사에서 독자가 이해하기 어려운 중요한 용어 5개를 선택해 주세요. "
                        + "각 용어의 정의와 기사 내에서의 맥락을 1~2문장으로 간단히 설명해 주세요. "
                        + "설명은 반드시 '~해요'체를 사용하고, 친절하고 명확하게 작성해 주세요. "
                        + "기사 내용은 다음과 같습니다: " + basenews.getDescription()));

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
    @Async
    public void generateAndSaveQuizzesForDailyNews(Basenews news) {

        System.out.println("generate quiz in ");

        // 이미 isDailynews=true인 basenews를 전달받음
        List<Map<String, String>> quizMessages = new ArrayList<>();
        quizMessages.add(Map.of("role", "system", "content",
                "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles."));
        quizMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사의 요약입니다. 이 요약을 바탕으로 기사에 대한 핵심 정보를 묻는 true/false 문제를 만들어 주세요. "
                        + "문제는 반드시 기사의 중요한 내용을 기반으로 해야 합니다. "
                        + "답은 O(참) 또는 X(거짓) 중 하나여야 하며, 문제의 정답과 관련된 배경 설명(해설)을 추가로 작성해주세요. "
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
        List<Map<String, String>> quizMessages = new ArrayList<>();
        quizMessages.add(Map.of("role", "system", "content",
                "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles."));
        quizMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사의 요약입니다."+
                       "해당 기사로 토론할만한 주제를 선정해주세요. 입문자 수준의 토론 주제로 너무 어렵지 않게 해주세요. 어려운 용어를 사용하지 않거나. "
                        +"사용할 시 쉽게 풀어서 제시 해주세요"
                        + "결과는 아래 형식에 맞춰 작성해 주세요:\n\n"
                        + "토픽:<토픽>\n"
                        + "기사 요약: " + news.getDescription()));


        String quizContent = (String) chatGPTService.generateContent(quizMessages).get("text");
        StringTokenizer st = new StringTokenizer(quizContent,":");
        st.nextToken();
        System.out.println(st.nextToken()+" is this topic of "+news.getTitle());
        System.out.println("think comment result");
        System.out.println(news.getTitle()+" : "+quizContent);

    }



    //*************************************************************
    //*************************************************************
    //*************************************************************
    //용어 파싱 메서드
    @Async
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


