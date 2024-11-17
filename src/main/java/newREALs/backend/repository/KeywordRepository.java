package newREALs.backend.repository;

import newREALs.backend.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword,Long> {


    Optional<Keyword> findByName(String key);
}
