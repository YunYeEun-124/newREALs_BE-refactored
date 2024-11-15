package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "keyword_id", nullable = true) //없는 경우도 있음.
    private Keyword keyword;


   // private List<HashMap<String,String>> term; //용어-설명세트 리스트
    @ElementCollection
    @CollectionTable(name = "BASENEWS_TERM_LIST", joinColumns = @JoinColumn(name = "basenews_id"))
    private List<TermDetail> termList = new ArrayList<>();

    @Column
    private LocalDateTime uploadDate;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true, length=1000)
    private String summary;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = true)
    private String newsUrl;


    @Column(nullable = false)
    private boolean isDailyNews; //매일 초기화.
    //T : 데일리 뉴스다~


    @Column(name = "likes_count", nullable = false)
    private int[] likesCounts=new int[3];

    @Builder
    public Basenews(String title,String summary,String description,LocalDateTime uploadDate,
                    String newsUrl,List<TermDetail> terms ,boolean isDailyNews,
                    Category category,SubCategory subCategory,Keyword keyword, String imageUrl,boolean scrapped){

        this.title = title;
        this.summary = summary;
        this.description = description;
        this.termList = terms;
        this.likesCounts=new int[]{0,0,0};  //basenews생성될 때 likeCounts 자동 초기화


    }

    //setter
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTermList(List<TermDetail> termList) {
        this.termList = termList;
    }


}

