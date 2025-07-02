package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import newREALs.backend.news.domain.SubCategory;
import newREALs.backend.accounts.domain.CurrentSubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CurrentSubInterestRepository extends JpaRepository<CurrentSubInterest,Long> {

    Optional<CurrentSubInterest> findByUserAndSubCategory(Accounts user, SubCategory subCategory);
    Optional<CurrentSubInterest> findByUserAndSubCategoryId(Accounts user, Long subCategoryId);
    // Category 별 quizCount
    @Query("SELECT SUM(si.quizCount) " +
            "FROM SubInterest si " +
            "JOIN si.subCategory sc " +
            "WHERE si.user.id = :userId AND sc.category.name = :category")
    Integer findQuizCountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    // Category 별 commentCount
    @Query("SELECT SUM(si.commentCount) " +
            "FROM SubInterest si " +
            "JOIN si.subCategory sc " +
            "WHERE si.user.id = :userId AND sc.category.name = :category")
    Integer findCommentCountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    // Category 별 scrapCount
    @Query("SELECT SUM(si.scrapCount) " +
            "FROM SubInterest si " +
            "JOIN si.subCategory sc " +
            "WHERE si.user.id = :userId AND sc.category.name = :category")
    Integer findScrapCountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT COALESCE(SUM(si.count), 0) " +
            "FROM SubInterest si " +
            "JOIN si.subCategory sc " +
            "WHERE si.user.id = :userId AND sc.category.name = :category")
    Integer findCountByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    // quizCount 총합
    @Query("SELECT COALESCE(SUM(si.quizCount), 0) " +
            "FROM SubInterest si " +
            "WHERE si.user.id = :userId")
    Integer findTotalQuizCountByUserId(@Param("userId") Long userId);

    //  commentCount 총합
    @Query("SELECT COALESCE(SUM(si.commentCount), 0) " +
            "FROM SubInterest si " +
            "WHERE si.user.id = :userId")
    Integer findTotalCommentCountByUserId(@Param("userId") Long userId);

    // 출석수
    @Query("SELECT COALESCE(SUM(si.attCount), 0) " +
            "FROM SubInterest si " +
            "WHERE si.user.id = :userId")
    Integer findTotalAttCountByUserId(@Param("userId") Long userId);

    
}
