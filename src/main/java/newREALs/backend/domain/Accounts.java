package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
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
@Setter
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
    @ElementCollection(fetch = FetchType.LAZY) //notion 참고
    @Cascade(org.hibernate.annotations.CascadeType.REMOVE)
    final boolean[] attendanceList = new boolean[31]; //매달 리셋됨

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "keywordInterest")
    @ElementCollection(fetch = FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.REMOVE)
    private List<Integer> keywordInterest = new ArrayList<>(Collections.nCopies(50, 0));



    @Builder
    public Accounts(String name, String profilePath, String email) {
        this.name = name;
        this.profilePath = profilePath;
        this.email = email;
        this.point = 0;
    }

    public void updateAttendance(int index) {
        attendanceList[index] = true;
        point += 5;

        for (boolean a : attendanceList) {
            System.out.print(a + " ");
        }

    }


    public void updateKeywordInterest(int keywordId, int change) {
        if(keywordId<1 || keywordId>keywordInterest.size()) {
            throw new IllegalArgumentException("keywordId는 1에서 50까지 입니다");
        }
        keywordInterest.set(keywordId - 1, keywordInterest.get(keywordId - 1) + change);
    }

}
