package newREALs.backend.repository;

import newREALs.backend.domain.Accounts;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Click;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClickRepository extends JpaRepository<Click,Long> {
    Optional<Click> findByUserAndBasenews(Accounts user, Basenews basenews);
}
