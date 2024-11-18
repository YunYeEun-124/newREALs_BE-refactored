package newREALs.backend.repository;

import jakarta.persistence.Column;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Quiz;
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

    Optional<String> findProblemByBasenewsId(Long basenewsId);

//
//    //카테고리에서 불러올때.
//    @Query("SELECT c.quiz FROM Quiz c WHERE c.baseNews.id = :basenews_id")
//    Optional<String> findProblemByBasenewsId(@Param("basenews_id") Long basenews_id);

}
