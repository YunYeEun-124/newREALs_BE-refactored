package newREALs.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import newREALs.backend.domain.Quiz;

@Getter
@Builder
@AllArgsConstructor
public class ProfileQuizStatusDTO {
    private Long userId;
    private Quiz[] quizList; // 퀴즈 리스트 (질문, 정답, 해설)
    private int[] quizStatus; //-1:틀림 0:안풀었음 1:맞음
}
