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
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final ChatGPTService chatGPTService;
    private final BaseNewsRepository basenewsRepository;
    private final QuizRepository quizRepository;
    private final InsightRepository insightRepository;
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    //요약, 설명, 용어, 퀴즈 생성 자동화

    @Transactional
    public void automaticBaseProcess(){
        //basenews들 중 summary=null인 뉴스들 가져옴(새롭게 생성된 뉴스)
        List<Basenews> newBasenews = basenewsRepository.findBySummaryIsNull();
        List<Basenews> resultList = new ArrayList<>();

        newBasenews.forEach(
                news -> {
                    try{
                        resultList.add( processArticle(news.getId()));
                    } catch (Throwable e){
                        log.error("Failed to process article ID: {}", news.getId(), e);
                    }
                }
        );

        basenewsRepository.saveAll(resultList);
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
    @Transactional
    public Basenews processArticle(Long basenewsId) throws Throwable {
        System.out.println("processArticle in ");
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid news ID"));

        //생성 통합하기
        List<Map<String,String>> Messages = new ArrayList<>();
        Messages.add(Map.of("role", "system", "content",
                "나는 뉴스 입문자들을 위해 뉴스를 쉽게 풀어 설명하고, 요약과 어려운 용어 설명을 제공해주는 사이트를 운영하고 있다. "
                        + "너는 뉴스 본문을 분석하고, 이를 통해 사용자가 쉽게 이해할 수 있도록 요약, 설명, 그리고 주요 용어 정리를 제공하는 전문적인 어시스턴트이다. "
                        + "Your task is to summarize the article, explain it clearly for readers without prior knowledge, and create a glossary of key terms for better understanding. "
                        + "Be concise, precise, and ensure your explanation is engaging and accessible for all readers."
        ));

        Messages.add(Map.of("role", "user", "content",
                "기사 본문 (Body): " + basenews.getDescription() + "\n\n"
                        + "다음의 세 가지 작업을 수행하라:\n\n"
                        + "1. 요약:\n"
                        + "- 기사 내용을 1-2줄 이내로 간결히 요약하라.\n"
                        + "- 사용자가 이 요약만 읽고도 기사의 핵심 내용을 파악할 수 있어야 한다.\n\n"
                        + "2. 설명:\n"
                        + "- 기사의 맥락과 주요 내용을 명확히 설명하라.\n"
                        + "- 배경 지식이 없는 독자도 이해할 수 있도록 친절하고 자세하게 작성하라.\n"
                        + "- 요약과는 구분되며, 명확하고 읽기 쉽게 작성해야 한다.\n\n"
                        + "3. 용어 정리:\n"
                        + "- 설명에서 사용된 어려운 용어나 독자가 이해하기 힘들 수 있는 핵심 용어를 1~5개 선택하여 정의하라.\n"
                        + "- 각 용어는 1-2문장으로 간단히 정의하며, 기사 내 맥락에서 어떻게 사용되었는지 포함하라.\n"
                        + "- 정의는 반드시 '~해요'체로 작성해야 한다.\n\n"
                        + "* 출력 형식:\n"
                        + "요약: [요약 내용]\n"
                        + "설명: [설명 내용]\n"
                        + "용어:\n"
                        + "1. [용어1]: [정의]\n"
                        + "2. [용어2]: [정의]\n"
                        + "...\n\n"
                        + "Example 1: \n"
                        + "요약: 일제강점기 피해자들이 일본 전범 기업에 대한 손해배상 소송에서 승소하며, 피해자들에게 배상금 지급 판결이 내려졌습니다.\n\n"
                        + "설명: 이 기사는 일제강점기 강제 동원 피해자들이 일본의 전범 기업을 상대로 제기한 손해배상 소송 결과에 대해 다루고 있습니다. 서울중앙지법은 일본제철과 미쓰비시중공업이 피해자들에게 배상금을 지급하라는 판결을 내렸으며, 이는 2018년 대법원 판결 이후 하급심에서도 피해자들의 권리를 인정한 사례입니다. 일본 기업들은 소멸시효를 주장했으나, 대법원의 판결로 이를 인정받지 못했습니다. 한국 정부의 제3자 변제 가능성에 대한 논의도 언급되었습니다.\n\n"
                        + "용어:\n"
                        + "1. 강제동원: 일제강점기에 일본 정부나 기업에 의해 한국인이 강제로 노동을 수행하게 만든 행위예요. 기사에서는 피해자들이 제철소와 항공기 제작소에서 일했다고 언급되었어요.\n"
                        + "2. 손해배상 소송: 피해를 입은 개인이 가해자에게 경제적 보상을 요구하는 법적 절차예요. 기사에서는 피해자들이 일본 전범 기업을 상대로 소송을 제기했어요.\n"
                        + "3. 소멸시효: 법률상 권리를 행사할 수 있는 기간이 지나면 그 권리를 상실하는 제도예요. 일본 기업이 이를 주장했으나, 대법원에서 받아들여지지 않았어요.\n"
        ));


        String result = (String) chatGPTService.generateContent(Messages).get("text");
        return parseBasenews(result,basenews);
    }

    //퀴즈 생성하는 메서드
    @Transactional
    public void generateAndSaveQuizzesForDailyNews(Basenews news) {
        // 이미 isDailynews=true인 basenews를 전달받음
        List<Map<String, String>> quizMessages = new ArrayList<>();
        quizMessages.add(Map.of("role", "system", "content",
                "You are a highly skilled assistant that generates quiz questions based on news articles. "
                        + "Your goal is to create meaningful True/False questions that highlight the key points of the articles."));
        quizMessages.add(Map.of("role", "user", "content",
                "다음은 뉴스 기사의 요약이다. 이 요약을 바탕으로 기사에 대한 핵심 정보를 묻는 true/false 문제를 만들어라. "
                        + "문제는 반드시 기사의 중요한 내용을 기반으로 해야 한다. "
                        + "정답은 반드시 true 혹은 false 중 하나로 명확하게 정해져야 한다. 문제의 정답과 관련된 배경 설명(해설)을 추가로 작성하라. " +
                        "해설은 한 줄로 간결하고 명확하게 작성하라."
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

    //용어,요약,설명 파싱 메서드
    @Async
    private Basenews parseBasenews(String termsContent,Basenews basenews) {
        List<TermDetail> termDetails = new ArrayList<>();
        String summary = "";
        String description = "";

        // "용어" 섹션만 추출
        String[] lines = termsContent.split("\\n");
        boolean isTermSection = false;

        for (String line : lines) {

            // 요약 섹션 처리
            if (line.startsWith("요약:")) {
                summary = line.substring(3).trim(); // "요약:" 이후 내용
            }
            // 설명 섹션 처리
            else if (line.startsWith("설명:")) {
                description = line.substring(3).trim(); // "설명:" 이후 내용
            }
            // "용어:"로 시작하는 섹션을 찾아 처리
            if (line.startsWith("용어:")) {
                isTermSection = true; // 용어 섹션 시작
                continue;
            }
            // 다른 섹션이 시작되면 중단
            if (isTermSection && line.trim().isEmpty()) {
                break;
            }
            // 번호와 내용이 있는 줄만 처리
            if (isTermSection && line.matches("^\\d+\\.\\s*.*")) {
                // 번호와 점 제거 (예: "1. " -> "")
                line = line.replaceFirst("^\\d+\\.\\s*", "");
                // 용어와 설명 분리
                String[] termAndDescription = line.split(":", 2); // 첫 번째 콜론 기준으로 분리
                if (termAndDescription.length == 2) {
                    String term = termAndDescription[0].trim();
                    String termDescription = termAndDescription[1].trim();
                    termDetails.add(new TermDetail(term, termDescription)); // TermDetail 객체 생성
                }
            }

        }


        basenews.setDescription(description);
        basenews.setSummary(summary);
        basenews.setTermList(termDetails);

        return basenews;
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
