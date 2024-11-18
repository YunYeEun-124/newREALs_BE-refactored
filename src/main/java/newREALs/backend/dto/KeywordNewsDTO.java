package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KeywordNewsDTO
{
    private List<String> userKeywords;
    private List<BaseNewsThumbnailDTO> baseNewsThumbnailDTOList;
    private int totalPage;
    private Long totalElement;
}