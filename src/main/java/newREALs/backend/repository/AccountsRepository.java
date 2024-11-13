package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {
    @Query("SELECT n.attendanceList FROM Accounts n WHERE n.id = :userId")
    List<Boolean> findAttendanceListByUserId(Long userId);
}
