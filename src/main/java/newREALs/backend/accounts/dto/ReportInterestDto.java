package newREALs.backend.accounts.dto;

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
    private int quiz;
    private int insight;
    private int scrap;
}