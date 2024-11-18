package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Quiz;
import newREALs.backend.domain.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface QuizStatusRepository extends JpaRepository<QuizStatus,Long> {
    Optional<QuizStatus> findByUser(Accounts user);

    Optional<QuizStatus> findByUserAndQuiz(Accounts user, Quiz quiz);
}
