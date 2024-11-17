package newREALs.backend.dto;

import lombok.Getter;

@Getter
public class QuizStatusDto {
    private Long quizId;
    private String problem;
    private Boolean answer;
    private String comment; //해설
    private Boolean state; //맞춤(true) 틀림(false) 안풂(null)
    private Long basenewsId;

    public QuizStatusDto(Long quizId, String problem, Boolean answer, String comment, Boolean isCorrect,Long basenewsId) {
        this.quizId = quizId;
        this.problem = problem;
        this.answer = answer;
        this.comment = comment;
        this.state = isCorrect;
        this.basenewsId=basenewsId;
    }
}
