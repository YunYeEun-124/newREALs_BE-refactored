package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private Basenews basenews;

    @Column(nullable=false)
    private Long count;
    @Builder
    public Click(Accounts user, Basenews basenews){
        this.user=user;
        this.basenews=basenews;
        this.count=1L;
    }

}
