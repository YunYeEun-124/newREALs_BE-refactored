package newREALs.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import newREALs.backend.domain.Basenews;

import java.time.LocalDateTime;
import java.util.List;
@Getter
public class NewsDetailDto {

    private Long id;
    private String title;
    private String summary;
    private String description;
    private String imageUrl;
    private String newsUrl;
    private String uploadDate;
    @JsonProperty("isScrapped")
    private boolean isScrapped;
    private String category;
    private String subCategory;
    private String keyword;
    private int good;
    private int bad;
    private int interesting;
    private int totalLikes;
    private Long viewCount; //조회수
    private List<TermDetailDto> termList;

    //
    private String insightTopic;

    private SimpleNewsDto prevNews;
    private SimpleNewsDto nextNews;
    private String wherePageFrom;

    public NewsDetailDto(Basenews basenews) {
        this.id = basenews.getId();
        this.title = basenews.getTitle();
        this.summary = basenews.getSummary();
        this.description = basenews.getDescription();
        this.imageUrl = basenews.getImageUrl();
        this.newsUrl = basenews.getNewsUrl();
        this.uploadDate = basenews.getUploadDate();
        this.viewCount=basenews.getViewCount();
        //공감수
        this.good=basenews.getLikesCounts()[0];
        this.bad=basenews.getLikesCounts()[1];
        this.interesting=basenews.getLikesCounts()[2];

        this.category=basenews.getCategory().getName();
        this.subCategory=basenews.getSubCategory().getName();
        this.keyword=basenews.getKeyword().getName();
    }

    public void setInsightTopic(String insightTopic) {
        this.insightTopic = insightTopic;
    }


    public void setTermList(List<TermDetailDto> termList) {
        this.termList = termList;
    }

    public List<TermDetailDto> getTermList() {
        return termList;
    }

    public int getTotalLikes() {
        return good+bad+interesting;
    }

    public void setScrapped(boolean isScrapped) {
        this.isScrapped=isScrapped;
    }

    public void setPrevNews(SimpleNewsDto prevNews) {
        this.prevNews = prevNews;
    }

    public void setNextNews(SimpleNewsDto nextNews) {
        this.nextNews = nextNews;
    }

    public void setWherePageFrom(String wherePageFrom) {
        this.wherePageFrom = wherePageFrom;
    }
}
