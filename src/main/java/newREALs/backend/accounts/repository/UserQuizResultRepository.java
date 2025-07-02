package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.Quiz;
import newREALs.backend.accounts.domain.UserQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserQuizResultRepository extends JpaRepository<UserQuizResult,Long> {
    Optional<UserQuizResult> findByUser(Accounts user);

    Optional<UserQuizResult> findByUserAndQuiz(Accounts user, Quiz quiz);
}
