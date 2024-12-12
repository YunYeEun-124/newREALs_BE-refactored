package newREALs.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import newREALs.backend.domain.Basenews;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class NewsDetailDto {

    private Long id;
    private String title;
    private String summary;
    private String description;
    private String imageUrl;
    private String newsUrl;
    private String uploadDate;
    private boolean scrapped;
    private String category;
    private String subCategory;
    private String keyword;

    private int totalLikes;
    private Long viewCount; //조회수
    @JsonIgnore
    private List<TermDetailDto> termList;
    private String insightTopic;

    private SimpleNewsDto prevNews;
    private SimpleNewsDto nextNews;
    private String wherePageFrom;
    private boolean dailynews;

    public NewsDetailDto(Basenews basenews,boolean dailynews) {
        this.id = basenews.getId();
        this.title = basenews.getTitle();
        this.summary = basenews.getSummary();
        this.description = basenews.getDescription();
        this.imageUrl = basenews.getImageUrl();
        this.newsUrl = basenews.getNewsUrl();
        this.uploadDate = basenews.getUploadDate();
        this.viewCount=basenews.getViewCount();
        //공감수
        this.totalLikes=basenews.getLikesCounts()[0]+basenews.getLikesCounts()[1]+basenews.getLikesCounts()[2];

        this.category=basenews.getCategory().getName();
        this.subCategory=basenews.getSubCategory().getName();
        this.keyword=basenews.getKeyword().getName();
        this.dailynews=dailynews;

    }




    public void setScrapped(boolean isScrapped) {
        this.scrapped=isScrapped;
    }

    public Map<String,String> termMap;
    public void setTermList(List<TermDetailDto> termList) {
        this.termList = termList;
        // termList를 Map<String, String>으로 변환
        this.termMap = termList.stream()
                .collect(Collectors.toMap(TermDetailDto::getTerm, TermDetailDto::getTermInfo));
    }

    public Map<String,String> getTermMap(){
        return termMap;
    }





}
