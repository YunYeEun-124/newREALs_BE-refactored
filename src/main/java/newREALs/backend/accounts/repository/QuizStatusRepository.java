package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.Quiz;
import newREALs.backend.accounts.domain.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface QuizStatusRepository extends JpaRepository<QuizStatus,Long> {
    Optional<QuizStatus> findByUser(Accounts user);

    Optional<QuizStatus> findByUserAndQuiz(Accounts user, Quiz quiz);
}
