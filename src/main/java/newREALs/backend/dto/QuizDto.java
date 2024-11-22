package newREALs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class QuizDto {
    private String  problem;
    private boolean answer;
    private String comment;
    @JsonProperty("isSolved")
    private boolean isSolved;

    public QuizDto(String problem, boolean answer, String comment, boolean isSolved) {
        this.problem = problem;
        this.answer = answer;
        this.comment = comment;
        this.isSolved = isSolved;
    }
}