package newREALs.backend.service;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.TermDetail;
import newREALs.backend.dto.GptRequestDto;
import newREALs.backend.repository.BasenewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NewsService {
    private final ChatGPTService chatGPTService;
    private final BasenewsRepository basenewsRepository;

    public NewsService(ChatGPTService chatGPTService, BasenewsRepository basenewsRepository) {
        this.chatGPTService = chatGPTService;
        this.basenewsRepository = basenewsRepository;
    }

    // GPT의 용어정리 파싱해서 TermDetail 리스트로 변환
//    private List<TermDetail> parseTerms(String termsContent) {
//        List<TermDetail> termDetails = new ArrayList<>();
//
//        // 예시: "용어1: 설명1, 용어2: 설명2, ..." 형식의 문자열을 파싱
//        String[] termsArray = termsContent.split(",");
//        for (String termPair : termsArray) {
//            String[] termAndDescription = termPair.split(":");
//            if (termAndDescription.length == 2) {
//                String term = termAndDescription[0].trim();
//                String termDescription = termAndDescription[1].trim();
//                termDetails.add(new TermDetail(term, termDescription));
//            }
//        }
//        return termDetails;
//    }
    private List<TermDetail> parseTerms(String termsContent) {
        List<TermDetail> termDetails = new ArrayList<>();

        // 줄바꿈을 기준으로 각 용어 설명 세트를 분리
        String[] termsArray = termsContent.split("\\n");
        for (String termPair : termsArray) {
            termPair = termPair.replaceAll("\\d+\\.\\s*", ""); // 번호 제거 (예: "1. ", "2. " 등)
            String[] termAndDescription = termPair.split(":", 2);  // 첫 번째 콜론을 기준으로 용어와 설명 구분
            if (termAndDescription.length == 2) {
                String term = termAndDescription[0].trim();
                String termDescription = termAndDescription[1].trim();
                termDetails.add(new TermDetail(term, termDescription));
            }
        }
        return termDetails;
    }


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
        basenews.setSummary(summary.length() > 255 ? summary.substring(0, 255) : summary);  // 길이 제한
        basenews.setDescription(explanation);  // 설명 필드에는 전체 설명 저장
        basenews.setTermList(termDetails);  // termList에 용어 리스트 저장
        basenewsRepository.save(basenews);
    }
}
