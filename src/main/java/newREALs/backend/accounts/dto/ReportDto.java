package newREALs.backend.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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

    @Getter @Setter
    @AllArgsConstructor
    public static class ResponseUserCommentListDTO {

        private List<userKeywordDto.UserCommentListDTO> insightList;
        private boolean hasNext;
        private boolean hasContent;
        private int nowPage;

    }
}
