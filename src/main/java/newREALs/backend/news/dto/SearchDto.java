package newREALs.backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class SearchDto {

    //밑에 카테고리 관련 전체 base뉴스
    private List<BaseNewsThumbnailDto> basenewsList ;
    private int totalPage;
    private Long totalElement;
}