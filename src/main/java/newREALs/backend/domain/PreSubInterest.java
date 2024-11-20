package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.HashMap;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreSubInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
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


    @Builder
    public PreSubInterest(Accounts user, SubCategory subCategory, int count){
        this.user = user;
        this.subCategory = subCategory;
        this.count = count;
    }
}