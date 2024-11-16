package newREALs.backend.repository;

import jakarta.persistence.Column;
import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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


}
