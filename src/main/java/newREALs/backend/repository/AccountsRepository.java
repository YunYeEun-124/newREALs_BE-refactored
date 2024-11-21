package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.dto.ProfileInterestProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {
    @Query("SELECT n.attendanceList FROM Accounts n WHERE n.id = :userId")
    List<Boolean> findAttendanceListById(Long userId);

    // LIMIT 사용 안됨 -> Pageable로 가져오는 개수 정하기
    // 카테고리별 관심도 count 상위 3개
    @Query(value = "SELECT c.name AS category, sc.name AS subCategory, SUM(si.count) AS count " +
            "FROM sub_interest si " +
            "JOIN sub_category sc ON si.sub_category_id = sc.id " +
            "JOIN category c ON sc.category_id = c.id " +
            "WHERE si.user_id = :userId AND c.name = :category " +
            "GROUP BY c.name, sc.name " +
            "ORDER BY SUM(si.count) DESC " +
            "LIMIT 3", nativeQuery = true)
    List<ProfileInterestProjection> findCategoryInterestById(Long userId, String category);


    // 전체 관심도 count 상위 3개
    @Query(value = "SELECT c.name AS category, sc.name AS subCategory, SUM(si.count) AS count " +
            "FROM sub_interest si " +
            "JOIN sub_category sc ON si.sub_category_id = sc.id " +
            "JOIN category c ON sc.category_id = c.id " +
            "WHERE si.user_id = :userId " +
            "GROUP BY c.name, sc.name " +
            "ORDER BY SUM(si.count) DESC " +
            "LIMIT 3", nativeQuery = true)
    List<ProfileInterestProjection> findTotalInterestById(Long userId);

    // 프로필 정보 수정
    @Query("SELECT a.name, a.profilePath " +
            "FROM Accounts a " +
            "WHERE a.id = :userId")
    List<Object[]> findNameAndProfilePathById(Long userId);


}