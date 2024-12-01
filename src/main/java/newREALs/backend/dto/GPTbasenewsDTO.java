package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import newREALs.backend.domain.TermDetail;

import java.util.List;

@Data
@AllArgsConstructor
public class GPTbasenewsDTO {
    private String title;
    private String description;
    private String summary;
    private List<TermDto> termList;

    @Data
    @AllArgsConstructor
    public static class TermDto {
        private String term;
        private String termInfo;
    }
}
