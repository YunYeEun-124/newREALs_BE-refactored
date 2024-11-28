package newREALs.backend.repository;

import newREALs.backend.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Modifying
    @Query(value = "INSERT INTO report (user_id, report) VALUES (:userId, CAST(:jsonData AS JSON))", nativeQuery = true)
    void saveReport(@Param("userId") Long userId, @Param("jsonData") String jsonData);

    @Modifying
    @Query(value = "UPDATE report SET report = CAST(:jsonData AS JSON) WHERE user_id = :userId", nativeQuery = true)
    void updateReport(@Param("userId") Long userId, @Param("jsonData") String jsonData);

    @Query(value = "SELECT COUNT(*) FROM report WHERE user_id = :userId", nativeQuery = true)
    int countByUserId(@Param("userId") Long userId);

    Optional<Report> findByUserId(Long userId);
}
