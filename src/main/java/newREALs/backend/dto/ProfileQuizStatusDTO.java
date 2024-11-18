package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProfileQuizStatusDTO {
    private Long user_id;
    private List<QuizDTO> quizList; // 퀴즈 리스트 (질문, 정답, 해설)
    private List<Integer> quizStatus; //-1:틀림 0:안풀었음 1:맞음
}
