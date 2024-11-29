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
    @Scheduled(cron="0 24 13 ? * * ")//매일 오전 6시 10분 실행
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
        System.out.println(resultList +" is saved");


    }

    @Scheduled(cron="0 30 13 ? * * ")//매일 오전 6시 10분 실행
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
        long startTime = System.nanoTime(); // 시작 시간 기록
        System.out.println("processArticle in ");
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid news ID"));

        //생성 통합하기
        List<Map<String,String>> Messages = new ArrayList<>();
        Messages.add(Map.of("role", "system", "content",
                "You are a professional assistant who specializes in summarizing, explaining, and defining terms in news articles. "
                        + "Your task is to create a summary, explanation, and list of terms for each article provided."));
        Messages.add(Map.of("role", "user", "content",
                "해야할 일이 크게 3가지이다. " + basenews.getDescription() +"에 대한 "+
                        "1. 1-2줄 이내로 짧게 요약한다. " +
                        "2. 핵심 배경, 사건의 원인과 결과를 포함하여 전체 내용을 한눈에 파악할 수 있도록 작성해주세요. 설명은 너무 간략하지 않게, 명확하면서도 친절하게 작성해 주세요. " +
                        "3. 독자가 이해하기 어려운 중요한 용어 5개를 선택한다."+
                        "각 용어의 정의와 기사 내에서의 맥락을 1-2문장으로 간단히 설명해야한다." +
                        "설명은 반드시 '~해요'체를 사용하고, 친절하고 명확하게 작성한다. " +
                        "출력 결과 예시를 보여줄게" +
                        "요약 : 연세대 수시 자연계 논술전형에서 문제 유출 사태로 인한 소송이 발생하고, 법정 공방으로 혼란이 커지고 있다. 합격자 발표 이후에도 사태 해결이 어려워져 수험생들의 혼란이 예상된다.\n" +
                        "설명 : 이 기사는 연세대 수시 자연계 논술전형에서 발생한 문제 유출 사태와 이에 따른 법정 공방에 대해 다루고 있습니다. 연세대는 재시험을 치루지 않고 현재 상황을 유지하고 있어 수험생들의 불안이 높아지고 있습니다. 합격자 발표 이후에도 사태의 해결이 어려워진다는 점이 주요 포인트입니다.\n" +
                        "용어 : \n" +
                        "1. 적정 난이도: 시험이나 문제의 난이도가 적절한 수준을 유지하는 것. 기사에서는 수능의 적정 난이도를 유지했다고 언급되었습니다.\n" +
                        "2. 의대 증원: 의학대학의 학생 모집 인원을 늘리는 것. 기사에서는 2025학년도 의대 증원 규모를 줄여야 한다는 주장에 대해 수용 불가 입장을 밝혔습니다.\n" +
                        "3. 킬러 문항: 시험에서 특히 어려운 문제로 수험생들을 골치 아프게 하는 문항. 기사에서는 킬러 문항을 배제하고 적정 난이도를 유지했다고 언급되었습니다.\n" +
                        "4. 사교육 카르텔: 사교육 기관들이 시장을 독점하거나 과도한 영향력을 행사하는 것. 기사에서는 사교육 카르텔을 척결하기 위해 노력했다고 언급되었습니다.\n" +
                        "5. 원천적으로 확보: 어떤 것이나 상황을 뿌리부터 확실하게 보호하거나 안전하게 만드는 것. 기사에서는 인터넷 주소 논란을 방지하기 위해 사이트를 원천적으로 확보하는 방안을 검토 중이라고 설명되었습니다.\n"
        ));

        String result = (String) chatGPTService.generateContent(Messages).get("text");


        // 처리 완료 시간 기록
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 밀리초로 변환


        return parseBasenews(result,basenews);
    }


    //퀴즈 생성하는 메서드
    @Transactional
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

        System.out.println("topic result : "+topic);
        System.out.println("aiComment result : "+aiComment);

        insightRepository.save(
          ThinkComment.builder()
                  .topic(topic)
                  .AIComment(aiComment)
                  .basenews(news).build()
        );

        System.out.println("insight created!");







        StringTokenizer st = new StringTokenizer(quizContent,":");
        st.nextToken();


    }


    //용어 파싱 메서드
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
