package newREALs.backend.accounts.domain;

import jakarta.persistence.*;
import lombok.*;
import newREALs.backend.news.domain.ThoughtComment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class UserThoughtComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thinkComment_id", nullable = false)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private ThoughtComment thoughtComment;

    @Column
    private String userComment;

    @Builder
    public UserThoughtComment(String userComment, Accounts user, ThoughtComment thoughtComment){
        this.userComment = userComment;
        this.thoughtComment = thoughtComment;
        this.user = user;
    }
}
