package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.SubCategory;
import newREALs.backend.domain.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SubInterestRepository extends JpaRepository<SubInterest,Long> {

    Optional<SubInterest> findByUserAndSubCategory(Accounts user, SubCategory subCategory);
    Optional<SubInterest> findByUserAndSubCategoryId(Accounts user, Long subCategoryId);
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
}