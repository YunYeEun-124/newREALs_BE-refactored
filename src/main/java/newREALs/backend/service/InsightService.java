package newREALs.backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.domain.*;
import newREALs.backend.dto.InsightDTO;
import newREALs.backend.dto.ResponseUserCommentDTO;
import newREALs.backend.dto.ResponseUserCommentListDTO;
import newREALs.backend.dto.UserCommentListDTO;
import newREALs.backend.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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

    //해당 뉴스의 인사이트가 없으면 200-null
    //g코멘트가 없으면 200-topic
    //해당 뉴스의 유저코멘트가 잇어면 200- topic, usercomment,aicomment

    public ResponseUserCommentDTO getUserInsight(Long userId,Long newsId){
        Basenews basenews = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        ResponseUserCommentDTO result;

        ThinkComment thinkComment = insightRepository.findByBasenews_Id(newsId)
                .orElse(null);
        if(thinkComment == null) return null ; //해당 뉴스에 인사이트 기능이 없음.

        String userComment =  userCommentRepository.findByThinkComment_IdAndUser_Id(thinkComment.getId(),userId);

        if(userComment == null){ //response 1. topic : userComment가 없는경우
            result = new ResponseUserCommentDTO(thinkComment.getTopic());
        }else{
           if(thinkComment.getPros() == null){//response 2. 댓글이 안 모인 경우
                result = new ResponseUserCommentDTO(thinkComment.getTopic(),userComment, thinkComment.getAIComment());
           }else {//response 3. 댓글이 모여서 찬,반,중으로 나눈 경우
               result = new ResponseUserCommentDTO(
                       thinkComment.getTopic(),
                       userComment,
                       thinkComment.getPros(),
                       thinkComment.getCons(),
                       thinkComment.getNeutral());
           }

        }

       return result;
    }

    @Transactional
    public String saveUserInsight(String userComment,Long userId, Long newsId){

        Basenews basenews = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));

        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        Optional<SubInterest> subInterest= subInterestRepository.findByUserAndSubCategory(user,basenews.getSubCategory());
        Optional<ThinkComment> thinkComment = insightRepository.findByBasenews_Id(newsId);

        if( thinkComment.isPresent() && subInterest.isPresent()){
            //해당 뉴스의 소카테고리 관심도 카운트 올리기.
            userCommentRepository.save(
                    UserComment.builder().
                    userComment(userComment).
                    user(user).
                    thinkComment(thinkComment.get()).
                    build());

            subInterest.get().updateCommentCount();

            return "user insight 저장 성공";

        }else throw  new IllegalArgumentException("해당 뉴스의 인사이트 기능이 없습니다.") ;

    }


    public List<InsightDTO> getInsightList(){

        List<InsightDTO> result = insightRepository.findAllBy();


        if(result.size() != 5){
            System.out.println("sth wrong. insight size is not 5");
            return null;
        }

        return result;
    }

    //사용자가 쓴 코멘트, 해당 토픽, 토픽에 대한 뉴스의 아이디
    public ResponseUserCommentListDTO getUserInsightList(Long userid, int page){
        Pageable pageable = getPageInfo(page);
        Slice<UserCommentListDTO> slice = userCommentRepository.findAllByUserId(userid,pageable);
        return new ResponseUserCommentListDTO(slice.getContent(),slice.hasNext(),slice.hasContent(),slice.getNumber()+1);

    }

    //common code 1
    public Pageable getPageInfo(int page){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        return  PageRequest.of(page-1,3,Sort.by(sorts));
    }
}
