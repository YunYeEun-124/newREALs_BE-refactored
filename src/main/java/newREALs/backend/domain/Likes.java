package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private LocalDateTime createdDate; //매달 분석도를 끊어야하기 때문.

    //중복방지,
    @ElementCollection
    @Column
    private boolean[] hasLiked = new boolean[3];

    //공감 수
    @ElementCollection
    @Column
    private int[] likes  = new int[3];

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private Basenews bnews;

    @Builder
    public  Likes(Basenews bnews, Accounts user, int index,LocalDateTime createdDate){
        this.bnews = bnews;
        this.user = user;
        this.createdDate = createdDate;

        if(index >= 0 && index <=2){
            hasLiked[index] = true;
            likes[index] ++;
        }else{
            throw new IllegalArgumentException("인덱스 범위가 오바임 ");
        }


    }


}
