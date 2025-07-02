package newREALs.backend.news.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "basenews_id", nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private Basenews basenews; //fk

    @Column(nullable = false)
    private String problem;//

    @Column(nullable = false)
    private Boolean answer; //정답 O -> T, X->F로 지정한다.

    @Column(nullable = false)
    private String comment; //해설

    @Builder
    public Quiz(String p, boolean a, String comment,Basenews basenews){
        this.answer = a;
        this.problem = p;
        this.comment = comment;
        this.basenews = basenews;
    }
}
