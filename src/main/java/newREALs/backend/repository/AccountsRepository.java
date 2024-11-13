package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {
    @Query("SELECT n.attendanceList FROM Accounts n WHERE n.id = :userId")
    List<Boolean> findAttendanceListByUserId(Long userId);

    @Query("SELECT d.quizList FROM Accounts a JOIN a.dailynews d WHERE a.id = :userId")
    List<Quiz> findQuizListByUserId(Long userId);

    @Query("SELECT d.quizStatus FROM Accounts a JOIN a.dailynews d WHERE a.id = :userId")
    List<Integer> findQuizStatusByUserId(Long userId);
}
