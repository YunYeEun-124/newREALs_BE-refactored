package newREALs.backend.news.dto;


import com.google.gson.annotations.Expose;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class newsInfo {
    //네이버 json에 맞춘 DTO, 윤예은 전용 DTO 다른 사람들은 신경안쓰셔도 됩니다.
    @Expose
    private String lastBuildDate; //필요없음.
    @Expose
    private int total; //
    @Expose
    private int start;
    @Expose
    private int display;
    @Expose
    private List<Item> items;

    @Getter @Setter
    public static class Item{
        // private String originalNewsArticle= "";
        // private String originalNewsImage="";
        @Expose
        private String title;


        @Expose
        private String originallink;
        @Expose
        private String link;

        @Lob
        @Expose
        private String description;
        @Expose
        private String pubDate;
    }

}