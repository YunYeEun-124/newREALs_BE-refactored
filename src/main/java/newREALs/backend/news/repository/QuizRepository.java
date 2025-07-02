package newREALs.backend.news.repository;

import newREALs.backend.news.domain.Basenews;
import newREALs.backend.news.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface QuizRepository extends JpaRepository<Quiz,Long> {
    Optional<Quiz> findByBasenews(Basenews basenews);

    List<Quiz> findTop5ByBasenewsIsDailyNewsTrueOrderByIdDesc();

    @Query("SELECT q.problem FROM Quiz q WHERE q.basenews.id = :basenewsId")
    Optional<String> findProblemByBasenewsId(@Param("basenewsId") Long basenewsId);

    boolean existsByBasenews(Basenews news);
}
