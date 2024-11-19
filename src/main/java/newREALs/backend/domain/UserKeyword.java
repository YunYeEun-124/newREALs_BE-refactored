package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "user_id", nullable = false)
    private Accounts  user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="keyword_id", nullable = false)
    private Keyword keyword;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="subCategory_id", nullable = false)
    private SubCategory subCategory;

    @Builder
    public UserKeyword(Accounts user, Keyword keyword,SubCategory sub){
        this.keyword = keyword;
        this.user = user;
        this.subCategory =sub;
    }
}