package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReportDto {
    Map<String, Object> change;
    Map<String, List<ReportInterestDto>> interest;
    Map<String, List<ReportCompareDto>> compare;
}
