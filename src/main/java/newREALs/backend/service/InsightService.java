package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.*;
import newREALs.backend.dto.InsightDTO;
import newREALs.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InsightService {

    private  final InsightRepository insightRepository;
    private final UserCommentRepository userCommentRepository;
    private final UserRepository userRepository;
    private final SubInterestRepository subInterestRepository;
    private final BaseNewsRepository baseNewsRepository;


    @Transactional
    public String saveUserInsight(String userComment,Long userId, Long newsId){

        Basenews basenews = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));

        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        Optional<SubInterest> subInterest= subInterestRepository.findByUserAndSubCategory(user,basenews.getSubCategory());
        Optional<ThinkComment> thinkComment = insightRepository.findByBasenews_Id(newsId);

        if( thinkComment.isPresent()){
            //해당 뉴스의 소카테고리 관심도 카운트 올리기.


            userCommentRepository.save(
                    UserComment.builder().
                    userComment(userComment).
                    user(user).
                    thinkComment(thinkComment.get()).
                    build());
            if(subInterest.isPresent()){
                subInterest.get().updateCommentCount();
            }else{

                SubInterest s = SubInterest.builder()
                        .user(user)
                        .subCategory(basenews.getSubCategory())
                        .count(0)
                        .scrapCount(0)
                        .quizCount(0)
                        .commentCount(1)
                        .build();
                subInterestRepository.save(s);

            }

            return "user insight 저장 성공";

        }else throw  new IllegalArgumentException("해당 뉴스의 인사이트 기능이 없습니다.") ;

    }


    public List<InsightDTO> getInsight(){

        List<InsightDTO> result = insightRepository.findAllBy();


        if(result.size() != 5){
            System.out.println("sth wrong. insight size is not 5");
            return null;
        }

        return result;
    }
}
