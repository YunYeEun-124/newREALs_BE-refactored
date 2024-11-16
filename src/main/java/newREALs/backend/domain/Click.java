package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    @Builder
    public Click(Accounts user, Basenews basenews){
        this.user=user;
        this.basenews=basenews;
    }

}
