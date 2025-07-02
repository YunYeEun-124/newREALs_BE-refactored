package newREALs.backend.news.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.Keyword;
import newREALs.backend.news.domain.SubCategory;
import newREALs.backend.accounts.domain.UserKeyword;
import newREALs.backend.news.repository.KeywordRepository;
import newREALs.backend.accounts.repository.UserKeywordRepository;
import newREALs.backend.accounts.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserKeywordService {
    //해당 키워드가 있는지 찾아야함.  -> 카
    //키워드 리스트 사이즈에 맞춰서 유저키워드객체 생성하게

    private final UserKeywordRepository userKeywordRepository;
    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;

    //레포지토리에 저장하는 함수 : 공통으로쓰임.
    public UserKeyword save(String keyword, Long userid){
        Optional<Accounts> user = userRepository.findById(userid);
        Keyword key = keywordRepository.getByName(keyword);
        SubCategory sub = key.getSubCategory();

        UserKeyword userKeyword = UserKeyword.builder()
                .user(user.get())
                .keyword(key)
                .sub(sub)
                .build();
        userKeywordRepository.save(userKeyword);

        return userKeyword;
    }

    //create userkeyword
    public List<String> createUserKeywords(List<String> keywords,Long userid){
        //해당하는 키워드 찾기.
        List<String> result = new ArrayList<>();
        Optional<Accounts> user = userRepository.findById(userid);

        if(userKeywordRepository.findAllByUserId(userid).isEmpty()){
            for(String keyword: keywords){

                Optional<Keyword> optionalKeyword = keywordRepository.findByName(keyword);
                //만약에 해당 키워드가 DB에 있으면 저장해라
                if(optionalKeyword.isPresent() && user.isPresent()) {
                    result.add(save(keyword,userid).getKeyword().getName());
                }
            }
        }



        return result;
    }

    //edit UPDATE
    @Transactional
    public List<String> updateUserKeywords(List<String> keywords, Long userId) {
        // 현재 사용자의 기존 키워드 조회
        List<String> currentKeywords = userKeywordRepository.findAllByUserId(userId).stream()
                .map(n -> n.getKeyword().getName())
                .toList();

        // 추가할 키워드 (새로운 키워드 중 기존에 없는 것)
        List<String> keywordsToAdd = keywords.stream()
                .filter(key -> !currentKeywords.contains(key))
                .toList();

        // 삭제할 키워드 (기존 키워드 중 새로운 키워드 리스트에 없는 것)
        List<String> keywordsToRemove = currentKeywords.stream()
                .filter(key -> !keywords.contains(key))
                .toList();

        // 키워드 추가
        for (String key : keywordsToAdd) {
            save(key, userId); // 새 키워드 저장
        }

        // 키워드 삭제
        for (String key : keywordsToRemove) {
            userKeywordRepository.deleteByUser_IdAndKeyword_Name(userId, key); // 기존 키워드 삭제
        }

        // 결과 리스트 반환 (최종 키워드 리스트)
        return keywords;
    }



}
