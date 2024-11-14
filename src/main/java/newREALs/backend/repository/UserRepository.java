package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Accounts,Long> {
}
