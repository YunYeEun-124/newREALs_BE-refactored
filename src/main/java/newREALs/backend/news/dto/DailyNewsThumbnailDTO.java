package newREALs.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class DailyNewsThumbnailDTO {
    private Long dailynewsId;
    private String title;
    private String imagePath;
    private String category;
    private String subCategory;
    private String keyword;
    private String quizQuestion;//추후 불러오기
}