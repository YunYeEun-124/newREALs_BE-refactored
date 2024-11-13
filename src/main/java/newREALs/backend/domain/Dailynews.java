package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dailynews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column
    @ElementCollection
    private Basenews[]  newsList = new Basenews[5];  // 5개의 뉴스를 담는 리스트, 담고나서 T로 변환

//    @Column
//    @ElementCollection
    @OneToMany(fetch = FetchType.LAZY)
    @OrderColumn(name = "quiz_order")
    private List<Quiz> quizList = new ArrayList<>();    // 각 뉴스마다 연결된 퀴즈 리스트

//    @Column
//    @ElementCollection
    @ElementCollection
    @OrderColumn(name = "quiz_status_order")
    private List<Integer> quizStatus = new ArrayList<>();  //-1:틀림 0:안풀었음 1:맞음

    @Builder
    public Dailynews(List<Quiz> quizList,Basenews[] newsList){
        this.quizList = quizList;

        if(newsList.length != 5) throw new IllegalArgumentException("dailynews는 5개입니다.");

        for(Basenews b : newsList){ //베이스뉴스필드 isdailynews가 T인 경우만 해당함.
           if( !b.isDailyNews() ) throw new IllegalArgumentException("basenews isDailynews가 F입니다.");
        }

        this.newsList = newsList;
    }

}
