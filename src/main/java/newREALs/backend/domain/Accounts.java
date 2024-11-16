package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** JPA 엔티티는 기본 생성자가 필수인데 이걸로 대체
 * 매개변수 필요한 생성자는 밑에있는 @builder 사용하여 가독성 향상
 * ( 팀프로젝트에 좋을거같아서 도입해봤습니다.)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)//notion 참고 바람.
public class Accounts {

    @Id //id를 기본키로 지정하겠다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 1씩 증가
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name="name",nullable = false)
    private String name;

    @Column(name = "profilePath")
    private String profilePath; //카톡 프로필 사진 경로 저장

    @Column(name = "point")
    @ColumnDefault("0")
    private int point;

    @Column(name = "attendanceList")
    @CollectionTable(name = "accounts_attendance_list", joinColumns = @JoinColumn(name ="account_id"))
    @ElementCollection(fetch = FetchType.LAZY)
    //private List<Boolean> attendanceList = new ArrayList<>(Collections.nCopies(31, false)); // 기본값으로 31개의 false 생성
    private boolean[] attendanceList = new boolean[31]; //매달 리셋됨

    @Column(name = "email", nullable = false, unique = true)
    private String email;


    @Builder
    public Accounts(String name, String profilePath, String email) {
        this.name = name;
        this.profilePath = profilePath;
        this.email = email;
        this.point = 0;
    }

    public void setPoint(int point) {
        this.point=point;
    }
}
