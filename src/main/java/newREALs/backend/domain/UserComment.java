package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class UserComment {

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
    private ThinkComment thinkComment;

    @Column
    private String userComment;

    @Builder
    public UserComment(String userComment,Accounts user,ThinkComment thinkComment){
        this.userComment = userComment;
        this.thinkComment = thinkComment;
        this.user = user;
    }
}
