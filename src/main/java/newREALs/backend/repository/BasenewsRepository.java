package newREALs.backend.repository;

import newREALs.backend.domain.Basenews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasenewsRepository extends JpaRepository<Basenews,Long> {
    List<Basenews> findByIsDailyNewsTrue();
}
