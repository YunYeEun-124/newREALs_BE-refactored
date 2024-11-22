package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.*;
import newREALs.backend.repository.*;
import org.aspectj.bridge.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserActionService {
    private final BaseNewsRepository basenewsRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;
    private final SubInterestRepository subInterestRepository;
    private final ScrapRepository scrapRepository;



    //스크랩 처리 메서드
    @Transactional
    public boolean getScrap(Long basenewsId, Long userId){
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        //유저관심도
        Optional<SubInterest> subInterest= subInterestRepository.findByUserAndSubCategory(user,basenews.getSubCategory());
        //스크랩 여부 확인
        Optional<Scrap> isScrapped=scrapRepository.findByUserAndBasenews(user,basenews);

        int keywordId = basenews.getKeyword().getId().intValue();

        //이미 스크랩 되어있던 거면 스크랩 해제
        if(isScrapped.isPresent()){
            scrapRepository.delete(isScrapped.get());
            //스크랩 해제 -> SubInterest 감소
            SubInterest s=subInterest.get();
            s.setCount(s.getCount()-2);
            s.setScrapCount(s.getScrapCount()-1);
            subInterestRepository.save(s);
            //스크랩 해제 -> KeywordInterest 감소
            user.updateKeywordInterest(keywordId, -2);
            userRepository.save(user);
            return false;
        } else{
            //스크랩 안되어있던 거면 스크랩 O
            Scrap newScrap=new Scrap(user,basenews);
            scrapRepository.save(newScrap);

            //스크랩 등록 -> KeywordInterest 증가
            user.updateKeywordInterest(keywordId, 2);
            userRepository.save(user);

            //스크랩 등록 -> SubInterest 증가
            if(subInterest.isPresent()){
                SubInterest s=subInterest.get();
                s.setCount(s.getCount()+3);
                s.setScrapCount(s.getScrapCount()+1);
                subInterestRepository.save(s);
            }else{
//                SubInterest s=new SubInterest(user,basenews.getSubCategory(),3);
                SubInterest s = SubInterest.builder()
                        .user(user)
                        .subCategory(basenews.getSubCategory())
                        .count(3)
                        .scrapCount(1)
                        .quizCount(0)
                        .commentCount(0)
                        .build();
                subInterestRepository.save(s);
            }
            return true;
        }

    }

    //공감 버튼 처리
    //reactionType : 좋아요 0 슬퍼오 1 흥미로워요 2
    @Transactional
    public String getLikes(Long basenewsId, Long userId, int reactionType ){
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        int keywordId=basenews.getKeyword().getId().intValue();


        if (reactionType < 0 || reactionType >= 3) {
            throw new IllegalArgumentException("Invalid reaction type. It must be between 0 and 2.");
        }
        //Likes 객체 받아와서
        Optional<Likes> existingLike=likesRepository.findByUserAndBasenews(user,basenews);
        Optional<SubInterest> subInterest=subInterestRepository.findByUserAndSubCategory(user,basenews.getSubCategory());
        String message;
        if(existingLike.isPresent()){
            //Likes 객체가 존재 : 이미 공감버튼 눌려 있다는 뜻
            Likes like=existingLike.get();
            if(like.getReactionType()==reactionType){ //화나요에 좋아요 눌러져있음 -> 또 화나요 클릭한 케이스
                basenews.getLikesCounts()[reactionType]--; //현재 반응 취소.
                //공감 해제 -> SubInterest 감소
                SubInterest s=subInterest.get();
                s.setCount(s.getCount()-1);
                subInterestRepository.save(s);

                //공감 해제 -> KeywordInterest 감소
                user.updateKeywordInterest(keywordId, -1);
                userRepository.save(user);
                likesRepository.delete(like);

                message="공감을 취소했습니다.";
            }else{ //화나요에 좋아요 눌러져있음 -> 좋아요 클릭한 케이스 : 아무일도 일어나지 않음
                message="이미 다른 공감버튼을 눌렀어요.";
            }
        }else{
            //Likes 객체 없음 : 공감 버튼 안눌려있음
            basenews.getLikesCounts()[reactionType]++; //공감수 증가
            message="공감 반영 완료";
            //공감 등록 -> KeywordInterest 증가
            user.updateKeywordInterest(keywordId,1);
            userRepository.save(user);
            //공감 등록 -> SubInterest 증가
            if(subInterest.isPresent()){
                SubInterest s=subInterest.get();
                if(reactionType<2){s.setCount(s.getCount()+1); } //좋아요, 슬퍼요는 +1
                else {s.setCount(s.getCount()+2);}  //흥미로워요는 +2
                subInterestRepository.save(s);
            }else{
//                SubInterest s=new SubInterest(user,basenews.getSubCategory(),1);
                SubInterest s = SubInterest.builder()
                        .user(user)
                        .subCategory(basenews.getSubCategory())
                        .count(1)
                        .scrapCount(0)
                        .quizCount(0)
                        .commentCount(0)
                        .build();
                if(reactionType==2)s.setCount(s.getCount()+1); //흥미로워요는 +2여야하니까..
                subInterestRepository.save(s);

            }
            likesRepository.save(new Likes(basenews,user,reactionType));
        }


        //저장
        basenewsRepository.save(basenews);
        return message;
    }

}
