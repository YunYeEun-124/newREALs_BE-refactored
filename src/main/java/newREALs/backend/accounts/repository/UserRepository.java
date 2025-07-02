package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Accounts, Long> {
    Optional<Accounts> findByEmail(String email); // email로 Accounts 조회
    Optional<Accounts> findById(Long userId);

    @Query("SELECT a.name FROM Accounts a WHERE a.id = :userId")
    String getNameByUserId(Long userId);
}
