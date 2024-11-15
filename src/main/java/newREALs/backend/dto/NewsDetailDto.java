package newREALs.backend.dto;

import lombok.Getter;
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
    private LocalDateTime uploadDate;
    private boolean isScrapped;
    private int good;
    private int bad;
    private int interesting;
    private int totalLikes;

    public NewsDetailDto(Basenews basenews) {
        this.id = basenews.getId();
        this.title = basenews.getTitle();
        this.summary = basenews.getSummary();
        this.description = basenews.getDescription();
        this.imageUrl = basenews.getImageUrl();
        this.newsUrl = basenews.getNewsUrl();
        this.uploadDate = basenews.getUploadDate();

        //공감수
        this.good=basenews.getLikesCounts()[0];
        this.bad=basenews.getLikesCounts()[1];
        this.interesting=basenews.getLikesCounts()[2];
    }

    private List<TermDetailDto> termList;

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
}
