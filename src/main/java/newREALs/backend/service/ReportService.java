package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.repository.KeywordRepository;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final KeywordRepository keywordRepository;

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
}
