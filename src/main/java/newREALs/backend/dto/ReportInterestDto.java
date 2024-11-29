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
    private int 퀴즈;
    private int 인사이트;
    private int 스크랩;
}