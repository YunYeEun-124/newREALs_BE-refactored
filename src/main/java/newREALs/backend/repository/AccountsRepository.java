package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
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
    @Query("SELECT sc.category.name, sc.name, si.count " +
            "FROM SubInterest si " +
            "JOIN si.subCategory sc " +
            "WHERE si.user.id = :userId AND sc.category.name = :category " +
            "ORDER BY si.count DESC")
    List<Object[]> findCategoryInterestById(Long userId, String category, Pageable pageable);


    // 전체 관심도 count 상위 3개
    @Query("SELECT si.subCategory.category.name, si.subCategory.name, si.count " +
            "FROM SubInterest si " +
            "WHERE si.user.id = :userId " +
            "ORDER BY si.count DESC")
    List<Object[]> findTotalInterestById(Long userId, Pageable pageable);

}