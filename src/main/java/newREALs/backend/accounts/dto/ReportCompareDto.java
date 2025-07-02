package newREALs.backend.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReportCompareDto {
    int quiz;
    int attendance;
    int insight;
}
