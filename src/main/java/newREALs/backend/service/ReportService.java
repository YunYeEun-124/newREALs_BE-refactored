package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.repository.KeywordRepository;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
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

        //미완성임
//    @Transactional
//    public void processReport(Long userId) throws Throwable{
//        //시작시간
//        long startTime = System.nanoTime();
//        System.out.println("processReport in ");
//        Accounts user=userRepository.findById(userId)
//                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
//
//        //1. 서현언니 분석 + 2.
//        List<Map<String,String>> Messages=new ArrayList<>();
//        Messages.add(Map.of("role","system","content",
//                "..."));
//
//        Messages.add(Map.of("role", "user", "content",
//                "해야할 일이 크게 2가지이다. "
//        ));
//
//        String result=(String) chatGPTService.generateContent(Messages).get("text");
//        System.out.println("gpt result");
//        System.out.println(result);
//
//        //처이 완료 시간
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime) / 1_000_000; // 밀리초로 변환
//
//        System.out.println("Execution time for processArticle: " + duration + " ms");
//    }
}
