package newREALs.backend.accounts.domain;

import jakarta.persistence.*;
import lombok.*;
import newREALs.backend.news.domain.Basenews;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNewsClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE) //accounts가 삭제될때 관련 scrap인스턴스 자동 삭제
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE) //basenews가 삭제될때 관련 scrap인스턴스 자동 삭제
    private Basenews basenews;

    @Column(nullable=false)
    private Long count;
    @Builder
    public UserNewsClick(Accounts user, Basenews basenews){
        this.user=user;
        this.basenews=basenews;
        this.count=1L;
    }

}
