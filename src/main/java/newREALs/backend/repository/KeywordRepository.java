package newREALs.backend.repository;

import newREALs.backend.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword,Long> {

    Keyword getByName(String keywordName);
    Optional<Keyword> findByName(String keywordName);
}