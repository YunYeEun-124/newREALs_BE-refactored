package newREALs.backend.domain;

/*	private User user;
	private SubCategory subCategory;
	private int count;(default = 0);*/

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashMap;

@Getter
@Entity
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PreSubInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subCategory_id", nullable = false)
    private SubCategory subCategory;

    @Column
    @ColumnDefault("0")
    private int count;

    @Column
    @ColumnDefault("0")
    private int quizCount;

    @Column
    @ColumnDefault("0")
    private int scrapCount;

    @Column
    @ColumnDefault("0")
    private int commentCount;

    @Column
    @ColumnDefault("0")
    private int attCount; // 출석수


    @Builder
    public PreSubInterest(Accounts user, SubCategory subCategory, int count, int quizCount, int scrapCount, int commentCount, int attCount) {
        this.user = user;
        this.subCategory = subCategory;
        this.count = count;
        this.quizCount = quizCount;
        this.scrapCount = scrapCount;
        this.commentCount = commentCount;
        this.attCount = attCount;
    }


}
