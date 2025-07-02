package newREALs.backend.news.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.accounts.domain.CurrentSubInterest;
import newREALs.backend.accounts.repository.CurrentSubInterestRepository;
import newREALs.backend.accounts.repository.UserRepository;
import newREALs.backend.news.dto.QuizDto;
import newREALs.backend.accounts.dto.UserQuizResultDto;
import newREALs.backend.news.domain.Basenews;
import newREALs.backend.news.domain.Quiz;
import newREALs.backend.accounts.domain.UserQuizResult;
import newREALs.backend.news.repository.BaseNewsRepository;
import newREALs.backend.news.repository.QuizRepository;
import newREALs.backend.accounts.repository.UserQuizResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final BaseNewsRepository basenewsRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final CurrentSubInterestRepository currentSubInterestRepository;

    //[post]퀴즈 풀기 :  맞았으면 true, 틀렸으면 false 반환
    @Transactional
    public Boolean solveQuiz(Long id, Long userId, Boolean userAnswer) {
        Basenews basenews = basenewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        Quiz quiz=quizRepository.findByBasenews(basenews)
                .orElseThrow(()->new EntityNotFoundException("이 뉴스에 속하는 퀴즈가 없습니다."));

        //이미 푼 퀴즈이면 다시 풀 수 없음.
        Optional<UserQuizResult> quizStatus= userQuizResultRepository.findByUserAndQuiz(user,quiz);
        if(quizStatus.isPresent()) throw new IllegalArgumentException("이미 푼 퀴즈는 다시 풀 수 없습니다.");

        if(quiz.getAnswer().equals(userAnswer)){
            userQuizResultRepository.save(new UserQuizResult(true,quiz,user));
            //정답 맞췄으니 포인트 획득
            user.setPoint(user.getPoint()+5);
            if (basenews.getKeyword() != null) {
                int keywordId = basenews.getKeyword().getId().intValue();
                user.updateKeywordInterest(keywordId, 2);
            }
            
            userRepository.save(user);

            CurrentSubInterest currentSubInterest = currentSubInterestRepository.findByUserAndSubCategory(user, basenews.getSubCategory())
                    .orElseGet(() -> CurrentSubInterest.builder()
                            .user(user)
                            .subCategory(basenews.getSubCategory())
                            .count(0) // 기본값 설정
                            .quizCount(0)
                            .scrapCount(0)
                            .commentCount(0)
                            .build()
                    );
            currentSubInterest.setCount(currentSubInterest.getCount() + 2);
            currentSubInterest.setQuizCount(currentSubInterest.getQuizCount() + 1);
            currentSubInterestRepository.save(currentSubInterest);

            return true;  //QuizStatus 객체 생성하고 저장
        }else{
            userQuizResultRepository.save(new UserQuizResult(false,quiz,user));
            if (basenews.getKeyword() != null) {
                int keywordId = basenews.getKeyword().getId().intValue();
                user.updateKeywordInterest(keywordId, 2);
            }
            
            userRepository.save(user);

            CurrentSubInterest currentSubInterest = currentSubInterestRepository.findByUserAndSubCategory(user, basenews.getSubCategory())
                    .orElseGet(() -> CurrentSubInterest.builder()
                            .user(user)
                            .subCategory(basenews.getSubCategory())
                            .count(0) // 기본값 설정
                            .quizCount(0)
                            .scrapCount(0)
                            .commentCount(0)
                            .build()
                    );

            currentSubInterest.setCount(currentSubInterest.getCount() + 2);
            currentSubInterest.setQuizCount(currentSubInterest.getQuizCount() + 1);
            currentSubInterestRepository.save(currentSubInterest);
            return false;
        }
    }

    //퀴즈 모두 맞히면 추가 10포인트 적립
    @Transactional
    public void checkExtraPoint(Long userId){
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));
        //오늘의 퀴즈 가져오기
        int count=0;
        List<Quiz> todayQuizzes=quizRepository.findTop5ByBasenewsIsDailyNewsTrueOrderByIdDesc();
        for(Quiz quiz:todayQuizzes){
            Optional<UserQuizResult> status= userQuizResultRepository.findByUserAndQuiz(user,quiz);
            if(status.isEmpty())break;
            else if(!status.get().getIsCorrect())break;
            else count+=1;

        }
        if(count==5){
            user.setPoint(user.getPoint()+10);
            userRepository.save(user);
        }
    }




    //[get] 프로필 페이지에서 퀴즈 현황 보기
    @Transactional
    public List<UserQuizResultDto> getQuizStatus(Long userId){
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        //오늘의 퀴즈 가져오기
        List<Quiz> todayQuizzes=quizRepository.findTop5ByBasenewsIsDailyNewsTrueOrderByIdDesc();
        //반환할 빈 배열 생성
        List<UserQuizResultDto> quizStatusList=new ArrayList<>();

        for(Quiz quiz:todayQuizzes){
            Optional<UserQuizResult>status= userQuizResultRepository.findByUserAndQuiz(user,quiz);
            Boolean isCorrect=status.map(UserQuizResult::getIsCorrect).orElse(null);

            UserQuizResultDto dto=new UserQuizResultDto(
                    quiz.getId(),
                    quiz.getProblem(),
                    quiz.getAnswer(),
                    quiz.getComment(),
                    isCorrect,
                    quiz.getBasenews().getId()

            );
            quizStatusList.add(dto);
        }
        return quizStatusList; //dto담은 리스트 반환
    }

    //[get] 뉴스 상세 페이지에서 퀴즈 보여주기
    @Transactional
    public QuizDto getQuiz(Long id, Long userId ){
        Basenews basenews = basenewsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다."));
        Accounts user=userRepository.findById(userId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다."));

        //메인 뉴스 아니면 퀴즈가 없으므로 null반환
        if(!basenews.isDailyNews())return null;


        Quiz quiz=quizRepository.findByBasenews(basenews)
                .orElseThrow(()->new IllegalStateException("메인 뉴스임에도 퀴즈가 존재하지 않습니다. 데이터 무결성을 확인하세요."));
        //메인뉴스이므로 퀴즈가 있어야 하는데 없는 상황=
        Optional<UserQuizResult> quizStatus= userQuizResultRepository.findByUserAndQuiz(user,quiz);

        boolean isSolved=quizStatus.isPresent();
        //QuizStatus가 존재 -> 이미 풀었다는 뜻 -> isSolved=true
        return new QuizDto(quiz.getProblem(),quiz.getAnswer(),quiz.getComment(),isSolved);

    }

}
