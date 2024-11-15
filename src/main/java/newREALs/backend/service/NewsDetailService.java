package newREALs.backend.service;

import jakarta.transaction.Transactional;
import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Likes;
import newREALs.backend.domain.Scrap;
import newREALs.backend.dto.NewsDetailDto;
import newREALs.backend.dto.TermDetailDto;
import newREALs.backend.repository.BasenewsRepository;
import newREALs.backend.repository.LikesRepository;
import newREALs.backend.repository.ScrapRepository;
import newREALs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NewsDetailService {
    private final BasenewsRepository basenewsRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final LikesRepository likesRepository;

    public NewsDetailService(BasenewsRepository basenewsRepository, UserRepository userRepository, ScrapRepository scrapRepository,LikesRepository likesRepository){
        this.basenewsRepository=basenewsRepository;
        this.userRepository=userRepository;
        this.scrapRepository=scrapRepository;
        this.likesRepository=likesRepository;
    }

//    public Basenews getNewsDetail(Long basenewsId){
//        return basenewsRepository.findById(basenewsId)
//                .orElseThrow(()->new IllegalArgumentException("Invalid news ID"));
//    }

    //뉴스 상세페이지 조회 메서드
    @Transactional
    public NewsDetailDto getNewsDetail(Long basenewsId, Long userId){
        Basenews basenews=basenewsRepository.findById(basenewsId)
                .orElseThrow(()->new IllegalArgumentException("Invalid news ID"));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Invalid user ID"));

        //basenews를 newsdetailDTO로 변환
        NewsDetailDto newsDetailDto=new NewsDetailDto(basenews);

        //용어 목록도 DTO로 변환
        List<TermDetailDto> termList=basenews.getTermList().stream()
                .map(TermDetailDto::new)
                .collect(Collectors.toList());
        newsDetailDto.setTermList(termList);

        //유저 스크랩여부 확인
        Optional<Scrap> isScrapped=scrapRepository.findByUserAndBasenews(user,basenews);
        boolean b=isScrapped.isPresent();
        newsDetailDto.setScrapped(b);

        return newsDetailDto;
    }

    //스크랩 처리 메서드
    @Transactional
    public void getScrap(Long basenewsId, Long userId){
        Basenews basenews=basenewsRepository.findById(basenewsId)
                .orElseThrow(()->new IllegalArgumentException("Invalid news ID"));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Invalid user ID"));

        //스크랩 여부 확인
        Optional<Scrap> isScrapped=scrapRepository.findByUserAndBasenews(user,basenews);

        //이미 스크랩 되어있던 거면 스크랩 해제
        if(isScrapped.isPresent()){
            scrapRepository.delete(isScrapped.get());
        } else{
            //스크랩 안되어있던 거면 스크랩 O
            Scrap newScrap=new Scrap(user,basenews);
            scrapRepository.save(newScrap);
        }

    }

    //공감 버튼 처리
    @Transactional
    public void getLikes(Long basenewsId, Long userId, int reactionType ){
        Basenews basenews=basenewsRepository.findById(basenewsId)
                .orElseThrow(()->new IllegalArgumentException("Invalid news ID"));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Invalid user ID"));

        if (reactionType < 0 || reactionType >= 3) {
            throw new IllegalArgumentException("Invalid reaction type. It must be between 0 and 2.");
        }
        //Likes 객체 받아와서
        Optional<Likes> existingLike=likesRepository.findByUserAndBasenews(user,basenews);

        if(existingLike.isPresent()){
            //Likes 객체가 존재 : 이미 공감버튼 눌려 있다는 뜻
            Likes like=existingLike.get();
            if(like.getReactionType()==reactionType){ //화나요에 좋아요 눌러져있음 -> 또 화나요 클릭한 케이스
                basenews.getLikesCounts()[reactionType]--; //현재 반응 취소. 공감수 감소
                likesRepository.delete(like);
            }else{ //화나요에 좋아요 눌러져있음 -> 좋아요 클릭한 케이스 : 아무일도 일어나지 않음

            }
        }else{
            //Likes 객체 없음 : 공감 버튼 안눌려있음
            basenews.getLikesCounts()[reactionType]++; //공감수 증가
            likesRepository.save(new Likes(basenews,user,reactionType));
        }
        //유저 관심도 업데이트 로직 추가하기!!!

        //저장
        basenewsRepository.save(basenews);
    }


}
