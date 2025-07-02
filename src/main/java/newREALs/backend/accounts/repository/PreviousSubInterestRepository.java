package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.PreviousSubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreviousSubInterestRepository extends JpaRepository<PreviousSubInterest, Long> {
    @Query("SELECT COALESCE(SUM(psi.count), 0) " +
            "FROM PreSubInterest psi " +
            "JOIN psi.subCategory sc " +
            "WHERE psi.user.id = :userId AND sc.category.name = :category")
    Integer findCountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    // quizCount 총합
    @Query("SELECT COALESCE(SUM(psi.quizCount), 0) " +
            "FROM PreSubInterest psi " +
            "WHERE psi.user.id = :userId")
    Integer findTotalQuizCountByUserId(@Param("userId") Long userId);

    //  commentCount 총합
    @Query("SELECT COALESCE(SUM(psi.commentCount), 0) " +
            "FROM PreSubInterest psi " +
            "WHERE psi.user.id = :userId")
    Integer findTotalCommentCountByUserId(@Param("userId") Long userId);

    // 출석수
    @Query("SELECT COALESCE(SUM(psi.attCount), 0) " +
            "FROM PreSubInterest psi " +
            "WHERE psi.user.id = :userId")
    Integer findTotalAttCountByUserId(@Param("userId") Long userId);
}
