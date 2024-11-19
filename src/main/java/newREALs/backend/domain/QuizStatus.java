package newREALs.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Accounts user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private boolean isCorrect;

    @Builder
    public QuizStatus(boolean isCorrect, Quiz quiz, Accounts user) {
        this.isCorrect = isCorrect;
        this.quiz = quiz;
        this.user = user;
    }

    public boolean getIsCorrect() {
        return isCorrect;
    }



}