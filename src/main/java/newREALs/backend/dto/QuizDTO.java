package newREALs.backend.dto;

import lombok.Getter;
import newREALs.backend.domain.Quiz;

@Getter
public class QuizDTO {
    private String quiz;
    private Boolean answer;
    private String comment;

    public QuizDTO(Quiz quiz) {
        this.quiz = quiz.getProblem();
        this.answer = quiz.getAnswer();
        this.comment = quiz.getComment();
    }
}
