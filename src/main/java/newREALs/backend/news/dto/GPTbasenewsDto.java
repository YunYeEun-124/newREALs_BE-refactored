package newREALs.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GPTbasenewsDto {
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
