package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReportInterestDto {
    private int percentage;
    private int quizCount;
    private int insightCount;
    private int scrapCount;
}