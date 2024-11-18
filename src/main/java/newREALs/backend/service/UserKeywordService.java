package newREALs.backend.service;

import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Keyword;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.domain.UserKeyword;
import newREALs.backend.repository.KeywordRepository;
import newREALs.backend.repository.UserKeywordRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserKeywordService {
    //해당 키워드가 있는지 찾아야함.  -> 카
    //키워드 리스트 사이즈에 맞춰서 유저키워드객체 생성학
    //

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
    public List<UserKeyword> createUserKeywords(List<String> keywords,Long userid){
        //해당하는 키워드 찾기.
        List<UserKeyword> result = new ArrayList<>();
        Optional<Accounts> user = userRepository.findById(userid);

        for(String keyword: keywords){

            Optional<Keyword> optionalKeyword = keywordRepository.findByName(keyword);
            //만약에 해당 키워드가 DB에 있으면 저장해라
            if(optionalKeyword.isPresent() && user.isPresent()) {
                result.add(save(keyword,userid));
            }
        }

        return result;
    }


    //edit UPDATE
    public List<String> updateUserKeywords(List<String> keywords, Long userid) {

        //새로 등록한 키워드 중에 기존 키워드가 없으면 그 키워드 삭제 함.

        List<String> currentKeyword = userKeywordRepository.findAllByUserId(userid).stream()
                .map( n-> n.getKeyword().getName())
                .toList();


        List<String> result = new ArrayList<>(currentKeyword);

        for(String key : keywords){
            if(userKeywordRepository.findByUser_IdAndKeyword_Name(userid,key).isEmpty()){ //새로 등록한 키워드가 기존에 없음 -> 추가해주기.
                save(key,userid); //repo에 저장
                result.add(key); //출력 리스트에 저장
            }

        }

        //기존 키워드 필요없는 것들 제거 .
        for(String key : result){
            if(!keywords.contains(key)){
                result.removeIf(keyword -> !keywords.contains(keyword));
                //delete
                userKeywordRepository.deleteByUser_IdAndKeyword_Name(userid,key);
            }
        }

        return result;
    }



}
