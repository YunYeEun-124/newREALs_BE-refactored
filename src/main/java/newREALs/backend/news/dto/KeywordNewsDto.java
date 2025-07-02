package newREALs.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KeywordNewsDto
{
    private List<String> userKeywords;
    private List<BaseNewsThumbnailDto> baseNewsList;
    private int totalPage;
    private Long totalElement;
}