package newREALs.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//썸네일.
@Getter @Setter
@Builder
@AllArgsConstructor
public class BaseNewsThumbnailDTO {

    private Long basenewsId;
    private String subCategory;
    private String category;
    private String keyword;
    private String title;
    private String summary;
    private String imageUrl;
    private String date;
    private boolean isScrap;



}