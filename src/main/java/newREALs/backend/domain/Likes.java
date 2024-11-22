package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column
    private String createdDate; //매달 분석도를 끊어야하기 때문.


    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "news_id", nullable = false)
    private Basenews basenews;

    private int reactionType;

    @Builder
    public  Likes(Basenews basenews, Accounts user, int reactionType){
        this.basenews = basenews;
        this.user = user;
        this.reactionType=reactionType;
        //this.createdDate = createdDate;
    }


}