package newREALs.backend.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//    private String category; //FK
//    private String subCategory; //FK
//    private String keyword; //FK
//    private Date   uploadDate;
//    private String imageUrl;
//    //	private Image image;
//    private String title;
//    private String summary;
//    private String description;
//    private String newsUrl;
//    private List<HashMap<String,String>> term; //용어-설명세트 리스트
//
//    private boolean scrap; //dafault = F -> T : 유저 스크랩리스트에 저장돼,
//    private boolean isDailyNews; //매일 초기화.
@Getter
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyword_id", nullable = true) //없는 경우도 있음.
    private Keyword keyword;


   // private List<HashMap<String,String>> term; //용어-설명세트 리스트
    @ElementCollection
    @Column
    private List<TermDetail> termList = new ArrayList<>();

    @Column
    private LocalDateTime uploadDate;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String newsUrl;

    @Column
    private boolean scrap;
    //dafault = F -> T : 유저 스크랩리스트에 저장돼,

    @Column
    private boolean isDailyNews; //매일 초기화.
    //T : 데일리 뉴스다~

    @Builder
    public Basenews(String title,String summary,String description,LocalDateTime uploadDate,
                    String newsUrl,List<TermDetail> terms ,
                    Category cate,SubCategory subCa,Keyword keyword, String imageUrl ){

        this.title = title;
        this.summary = summary;
        this.description = description;
        this.uploadDate = uploadDate;
        this.newsUrl = newsUrl;
        this.isDailyNews = false;
        this.scrap = false;
        this.category = cate;
        this.subCategory = subCa;
        this.keyword  = keyword;
        this.imageUrl = imageUrl;
        this.termList = terms;


    }

}

