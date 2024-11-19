package newREALs.backend.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.jsoup.Connection;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Getter
@Entity
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Basenews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="category_id",nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="subCategory_id",nullable = false)
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "keyword_id", nullable = true) //없는 경우도 있음.
    private Keyword keyword;


    // private List<HashMap<String,String>> term; //용어-설명세트 리스트
    @ElementCollection
    @CollectionTable(name = "BASENEWS_TERM_LIST", joinColumns = @JoinColumn(name = "basenews_id"))
    private List<TermDetail> termList = new ArrayList<>();

    @Column
    private String uploadDate;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true, length=1000)
    private String summary;

   // @Lob
    //@Column(nullable = false)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private String newsUrl;

    @Column(nullable=false)
    private Long viewCount;


    @Column(name = "is_daily_news",nullable = false)
    private boolean isDailyNews; //매일 초기화.
    //T : 데일리 뉴스다~

    @Column(name = "likes_count", nullable = false)
    private int[] likesCounts=new int[3];

    //예은
    @Builder
    public Basenews(String title,String newsUrl,String imageUrl,String uploadDate,String description,Keyword keyword,SubCategory subCategory,Category category,
                    boolean isDailyNews){
        this.title = title;
        this.description = description;
        this.uploadDate = uploadDate;
        this.newsUrl = newsUrl;
        this.isDailyNews = isDailyNews;
        this.category = category;
        this.subCategory = subCategory;
        this.keyword  = keyword;
        this.imageUrl = imageUrl;
     //   this.scrap = false;
        this.termList = new ArrayList<>();
        this.likesCounts=new int[]{0,0,0};  //basenews생성될 때 likeCounts 자동 초기화
        this.viewCount=0L;  //기본값 0으로 설정
    }

    //현진
    @Builder
    public Basenews(String title,String summary,String description,String uploadDate,
                    String newsUrl,List<TermDetail> terms ,boolean isDailyNews,
                    Category category,SubCategory subCategory,Keyword keyword, String imageUrl,boolean scrapped){

        this.title = title;
        this.summary = summary;
        this.description = description;
        this.uploadDate = uploadDate;
        this.newsUrl = newsUrl;
        this.category = category;
        this.subCategory = subCategory;
        this.keyword  = keyword;
        this.imageUrl = imageUrl;
        this.termList = terms;
        this.isDailyNews = isDailyNews;
        this.likesCounts=new int[]{0,0,0};  //basenews생성될 때 likeCounts 자동 초기화
        this.viewCount=0L;  //기본값 0으로 설정

    }

    public void cancelDailyNews(){
        this.isDailyNews = false;
    }


    public void checkDailyNews(){
        this.isDailyNews = true;
    }

}
