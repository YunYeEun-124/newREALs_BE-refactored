package newREALs.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KeywordNewsDTO
{
    private List<String> userKeywords;
    private List<BaseNewsThumbnailDTO> baseNewsList;
    private int totalPage;
    private Long totalElement;
}