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

    //이 메서드 이름 분석해서 JPA가 적절한 쿼리문을 자동생성함
//    SELECT q.*
//    FROM quiz q
//    INNER JOIN basenews b ON q.basenews_id = b.id
//    WHERE b.is_daily_news = true
//    ORDER BY q.id DESC
//    LIMIT 5;

    //카테고리에서 불러올때.
    @Query("SELECT c.quiz FROM Quiz c WHERE c.baseNews.id = :basenews_id")
    Optional<String> findQuizByBaseNewsId(@Param("basenews_id") Long basenews_id);

}
