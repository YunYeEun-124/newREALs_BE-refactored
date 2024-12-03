package newREALs.backend.repository;

import newREALs.backend.domain.Basenews;
import newREALs.backend.domain.ThinkComment;
import newREALs.backend.dto.InsightDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

public interface InsightRepository extends JpaRepository<ThinkComment,Long> {



    @Query("SELECT new newREALs.backend.dto.InsightDTO(tc.topic, tc.basenews.category.name, tc.basenews.id) " +
            "FROM ThinkComment tc " +
            "WHERE tc.basenews.isDailyNews = true")
    List<InsightDTO> findAllBy();

    @Query("select tc from ThinkComment tc where tc.basenews.id = :newsId")
    Optional<ThinkComment> findByBasenews_Id(@Param("newsId") Long newsId);


    boolean existsByBasenews(Basenews news);
}
