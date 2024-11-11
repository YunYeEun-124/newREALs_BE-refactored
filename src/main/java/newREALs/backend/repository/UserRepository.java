package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Accounts, Long> {
    Optional<Accounts> findByEmail(String email); // email로 Accounts 조회
}
