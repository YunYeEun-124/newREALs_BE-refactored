package newREALs.backend.service;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.TermDetail;
import newREALs.backend.repository.BaseNewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ArticleProcessingService {
    private final ChatGPTService chatGPTService;
    private final BaseNewsRepository basenewsRepository;
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    public ArticleProcessingService(ChatGPTService chatGPTService, BaseNewsRepository basenewsRepository) {
        this.chatGPTService = chatGPTService;
        this.basenewsRepository = basenewsRepository;

    }

    @Async("taskExecutor")
    public CompletableFuture<Basenews> processArticleAsync(Long id) {
        try {
            return CompletableFuture.completedFuture(processArticle(id));
        } catch (Throwable e) {
            log.error("Failed to process article ID: {}", id, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public Basenews processArticle(Long basenewsId) throws Throwable {
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
                        "3. 독자가 이해하기 어려운 핵심 용어를 선택한다. 개수는 최소 1개 최대 5개이다."+
                        "이 용어는 2번에서 생성한 설명에 포함되어 있어야 한다. 또한 2번에서 생성한 설명에 등장하는 순서대로 용어를 반환해야한다."+
                        "각 용어의 정의와 기사 내에서의 맥락을 1-2문장으로 간단히 설명해야한다." +
                        "설명은 반드시 '~해요'체를 사용하고, 친절하고 명확하게 작성한다. " +
                        "요약 : 일제강점기 피해자들이 일본 전범 기업에 대한 손해배상 소송에서 잇따라 승소하였으며, 일본제철과 미쓰비시중공업은 각각 피해자들에게 배상금을 지급하라는 판결을 받았습니다.\n" +
                        "설명 : 이 기사는 일제강점기 강제 동원된 피해자들이 일본의 전범 기업들을 상대로 제기한 손해배상 소송의 결과에 대해 다루고 있습니다. 서울중앙지법에서는 일본제철과 미쓰비시중공업이 각각 피해자들에게 1억 원을 배상하라는 판결을 내렸습니다. 이는 대법원이 2018년에 강제동원 피해자들의 손해배상 책임을 인정한 이후, 하급심에서도 피해자들의 청구권을 인정하는 판결이 이어진 결과입니다. 일본 기업 측은 소멸시효를 주장했으나, 대법원의 판결로 인해 이 주장은 받아들여지지 않았습니다. 김영환 민족문제연구소 대외협력실장은 배상금 변제 방법에 대해 언급하며, 한국 정부의 제3자 변제에 대한 실현이 없음을 지적하고, 필요하다면 강제집행을 고려하고 있다고 밝혔습니다.\n" +
                        "용어 : \n" +
                        "1. 강제동원: 일제강점기에 일본 정부나 기업에 의해 한국인이 일본 내외의 공장, 광산 등에서 강제로 노동을 수행하게 만든 행위입니다. 기사에서는 피해자들이 강제동원되어 일본의 제철소와 항공기 제작소에서 일했다고 언급되었습니다.\n" +
                        "2. 손해배상 소송: 피해를 입은 개인이 가해자에게 그 피해에 대한 경제적 보상을 요구하는 법적 절차입니다. 이 기사에서는 일제강점기 피해자들이 일본 전범 기업을 상대로 손해배상을 청구했습니다.\n" +
                        "3. 소멸시효: 법률상 권리를 행사할 수 있는 기간이 지나면 그 권리를 상실하게 되는 제도입니다. 일본 기업 측은 강제징용 피해 배상 청구권에 대해 소멸시효를 주장했으나, 법원은 이를 받아들이지 않았습니다.\n" +
                        "4. 제3자 변제: 채무자가 아닌 다른 사람이 채무자 대신에 채권자에게 채무를 변제하는 것입니다. 기사에서는 한국 정부가 제3자로서 배상금을 지급할 계획이었으나, 이행된 경우가 없다고 언급되었습니다.\n"
        ));

        String result = (String) chatGPTService.generateContent(Messages).get("text");
        return parseBasenews(result,basenews);
    }

    //용어,요약,설명 파싱 메서드
    @Async
    public Basenews parseBasenews(String termsContent,Basenews basenews) {
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
}
