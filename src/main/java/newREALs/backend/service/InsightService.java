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
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InsightService {

    private  final InsightRepository insightRepository;
    private final UserCommentRepository userCommentRepository;
    private final UserRepository userRepository;
    private final SubInterestRepository subInterestRepository;
    private final BaseNewsRepository baseNewsRepository;
    private final ChatGPTService chatGPTService;
    //해당 뉴스의 인사이트가 없으면 200-null
    //g코멘트가 없으면 200-topic
    //해당 뉴스의 유저코멘트가 잇어면 200- topic, usercomment,aicomment


    //pros,cons,neutral
    public boolean gatherOpinions(ThinkComment thinkComment){
        if(thinkComment.getPros() != null | thinkComment.getCons() != null || thinkComment.getNeutral() != null)//이미 채워져있다면?
            return true;

        List<String> userComments = userCommentRepository.findByThinkComment_Id(thinkComment.getId());

        System.out.println("userComment size is : "+userComments.size());
        if(userComments.size() >= 3){
            //gpt 연동
            List<Map<String, String>> titleMessages = new ArrayList<>();

            titleMessages.add(Map.of("role", "system", "content", userComments+"를 찬성(pros),반성(cons),중립(neutral)으로 나눠야해. "));
            titleMessages.add(Map.of("role", "user", "content", thinkComment.getTopic()+"에 대한 사람들의 의견을 모았어. 이걸 찬성,반대,중립 세 가지로 분류하고 요약해줘" +
                    "너의 의도 " +
                    "1. 사람들의 댓글 중 비속어,의미없는 댓글들을 안보이게 하기\n" +
                    "2. 다른 입장도 생각해보기 \n" +
                    "출력 형식은 반드시 지킬것 \n. " +
                    "pros:  \n" +
                    "cons: \n"+
                    "neutral: \n" +
                    "말투는 ~해요.~라고 생각해요.라고 해"+
                    "만약 찬성,반대,중립 으로 나눌 수 없는 의견들이라면 해당 파트에 null라고 표시\n" +
                    "댓글 중 찬성, 반대, 중립으로 분류했을 때, 특정 분류에 해당하는 댓글이 없으면 해당 분류를 반드시 'null'로 표시\n"
            ));
            String opinions = (String) chatGPTService.generateContent(titleMessages).get("text");
            System.out.println("opinions is "+opinions);
            // GPT 응답 파싱
            String pros = extractSection(opinions, "pros");
            String cons = extractSection(opinions, "cons");
            String neutral = extractSection(opinions, "neutral");

            // ThinkComment에 값 설정
            thinkComment.setPros(pros);
            thinkComment.setCons(cons);
            thinkComment.setNeutral(neutral);
            insightRepository.save(thinkComment);
            return true;
        }else return false;
    }

    private String extractSection(String text, String section) {
        Pattern pattern = Pattern.compile(section + ":\\s*(.*?)(\\n\\n|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null; // 해당 섹션이 없을 경우 null로 반환
    }

    public ResponseUserCommentDTO getUserInsight(Long userId,Long newsId){
        Basenews basenews = baseNewsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        ResponseUserCommentDTO result;

        ThinkComment thinkComment = insightRepository.findByBasenews_Id(newsId)
                .orElse(null);
        if(thinkComment == null) return null ; //해당 뉴스에 인사이트 기능이 없음.

        String userComment =  userCommentRepository.findByThinkComment_IdAndUser_Id(thinkComment.getId(),userId);
        boolean isGathered = gatherOpinions(thinkComment);

        if(userComment == null){ //response 1. topic just
            result = new ResponseUserCommentDTO(thinkComment.getTopic());
        }else{

           if(!isGathered){//response 2. 댓글이 안 모인 경우
               System.out.println("gather is false ");
                result = new ResponseUserCommentDTO(thinkComment.getTopic(),userComment, thinkComment.getAIComment());
           }else {//response 3. 댓글이 모여서 찬,반,중으로 나눈 경우
               System.out.println("댓글 3개 이상 모임! ");
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
        return  PageRequest.of(page-1,4,Sort.by(sorts));
    }
}
