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
 //   private final

    //스크랩 처리 메서드
    @Transactional
    public boolean getScrap(Long basenewsId, Long userId){
        Basenews basenews = basenewsRepository.findById(basenewsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        //유저관심도, 스크랩
        Optional<SubInterest> subInterest= subInterestRepository.findByUserAndSubCategory(user,basenews.getSubCategory());
        Optional<Scrap> isScrapped=scrapRepository.findByUserAndBasenews(user,basenews);
        int keywordId = basenews.getKeyword().getId().intValue();

        //이미 스크랩 되어있던 거면 스크랩 해제
        if(isScrapped.isPresent()){
            scrapRepository.delete(isScrapped.get());

            subInterest.get().updateScrapCount(-1);//스크랩 해제- > SubInterest 감소
            subInterest.get().updateTotalCount(-2);
            user.updateKeywordInterest(keywordId, -2); //스크랩 해제 -> KeywordInterest 감소
            return false;

        } else{//스크랩 안되어있던 거면 스크랩 O
            Scrap newScrap=new Scrap(user,basenews);
            scrapRepository.save(newScrap);

            user.updateKeywordInterest(keywordId, 2);//스크랩 등록 -> KeywordInterest 증가
            subInterest.get().updateScrapCount(1); //스크랩 등록 -> SubInterest 증가
            subInterest.get().updateTotalCount(3);

            return true;
        }

    }

    //공감 버튼 처리
    //reactionType : 좋아요 0 슬퍼오 2 흥미로워요 1
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
                subInterest.get().updateTotalCount(-1); //공감 해제 -> SubInterest 감소
                user.updateKeywordInterest(keywordId, -1); //공감 해제 -> KeywordInterest 감소
                likesRepository.delete(like);
                message="공감을 취소했습니다.";
            }else{ //화나요에 좋아요 눌러져있음 -> 좋아요 클릭한 케이스 : 아무일도 일어나지 않음
                message="공감 변경 완료";
                if(like.getReactionType()==1)subInterest.get().updateTotalCount(-1);
                else if(reactionType==1)subInterest.get().updateTotalCount(1);
                like.setReactionType(reactionType);
                likesRepository.save(like);
            }
        }else{//Likes 객체 없음 : 공감 버튼 안눌려있음
            basenews.getLikesCounts()[reactionType]++; //공감수 증가
            likesRepository.save(new Likes(basenews,user,reactionType)); //인스턴스 생성
            message="공감 반영 완료";
            user.updateKeywordInterest(keywordId,1); //공감 등록 -> KeywordInterest 증가
//            if(reactionType<2)subInterest.get().updateTotalCount(1); //공감 등록 -> SubInterest 증가
//            else subInterest.get().updateTotalCount(2);
            if(reactionType==1)subInterest.get().updateTotalCount(2);
            else subInterest.get().updateTotalCount(1);
        }
        basenewsRepository.save(basenews);
        return message;
    }

}
