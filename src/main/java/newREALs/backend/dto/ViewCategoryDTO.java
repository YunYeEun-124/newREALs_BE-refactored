package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

//category, subcategory view 불러올대.


@Getter @Setter @AllArgsConstructor
public class ViewCategoryDTO {

    //daily하나
    private DailyNewsThumbnailDTO dailynews;
    //밑에 카테고리 관련 전체 base뉴스
    private List<BaseNewsThumbnailDTO> basenewsList ;
    //private Page<BaseNewThumbnailDTO> basesnewsList;
    private int totalPage;
    private Long totalElement;
}